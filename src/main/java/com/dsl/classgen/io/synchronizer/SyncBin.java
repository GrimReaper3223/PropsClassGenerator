package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.Parsers;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

/**
 * The Class SyncBin.
 */
public final class SyncBin implements SyncOperations, Parsers {

	private final ClassFile cf = ClassFile.of();
	private ClassModel cm;

	/**
	 * Writes data to a contextualized file, requesting only the byte array for this
	 * operation.
	 */
	private Consumer<byte[]> consumeWriter = data -> Writer.write(pathsCtx.getOutputClassFilePath(), data);

	/**
	 * Performs data writing by requesting the file path and the byte array for this
	 * operation.
	 */
	private BiConsumer<Path, byte[]> biConsumeWriter = Writer::write;

	/**
	 * Instantiates a new sync bin.
	 */
	public SyncBin() {
		try {
			cm = cf.parse(pathsCtx.getOutputClassFilePath());
		} catch (IOException e) {
			Utils.handleException(e);
		}
	}

	/**
	 * Insert class section to the compiled class file. Perform operation in a batch
	 * manner, processing a list of paths
	 *
	 * @param pathList the path list to process
	 */
	@Override
	public void insertClassSection(List<Path> pathList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new compiled data entries...");
		List<InnerStaticClassModel> classModelList = pathList.stream().map(OutterClassModel::getModel).toList();

		classModelList.forEach(classModel -> {

			byte[] bytes = cf.build(ClassDesc.of(classModel.className()), classBuilder -> classBuilder
					.withSuperclass(cm.thisClass().asSymbol()).withFlags(classModel.byteCodeModifiers())
					.with(buildInnerClassAnnotation(classModel.annotationMetadata().filePath(),
							classModel.annotationMetadata().javaType(), classModel.annotationMetadata().hash()))
					.withMethod(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PRIVATE,
							mtdBuilder -> mtdBuilder.with(RuntimeVisibleAnnotationsAttribute.of(Annotation
									.of(GeneratedPrivateConstructor.class.describeConstable().orElseThrow())))));

			ClassModel innerClassFileModel = cf.parse(bytes);

			// gera os campos internos
			ByteBuffer bb = ByteBuffer.wrap(cf.build(innerClassFileModel.thisClass().asSymbol(), classBuilder -> {
				innerClassFileModel.elementStream().forEach(classBuilder::with);

				classModel.fieldModelList()
						.forEach(fieldModel -> classBuilder.withField(fieldModel.fieldName(),
								fieldModel.fieldType().describeConstable().orElseThrow(),
								fieldBuilder -> fieldBuilder
										.with(buildInnerFieldAnnotation(fieldModel.annotationMetadata().key(),
												fieldModel.annotationMetadata().hash()))
										.with(ConstantValueAttribute.of(fieldModel.rawFieldValue().toString()))
										.withFlags(fieldModel.byteCodeModifiers())));
			}));

			// escreve os novos dados da classe interna na outter class
			consumeWriter.accept(bb.array());
		});
	}

	/**
	 * Erase class section from the compiled class file. Perform operation in a
	 * batch manner, processing a list of cache models
	 *
	 * @param currentCacheModelList the current cache model list to process
	 */
	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing compiled class section...");
		List<Path> fileNameList = currentCacheModelList.stream().<Path>mapMulti((model, consumer) -> {
			try {
				consumer.accept(Utils.convertSourcePathToClassPath(model.filePath));
			} catch (ClassNotFoundException e) {
				Utils.handleException(e);
			}
		}).toList();

		byte[] newBytes = cf.build(cm.thisClass().asSymbol(),
				classBuilder -> cm.attributes().stream().filter(InnerClassesAttribute.class::isInstance)
						.map(InnerClassesAttribute.class::cast).flatMap(attr -> attr.classes().stream())
						.filter(elem -> !fileNameList.contains(Path.of(elem.innerClass().name().stringValue())))
						.forEach(elem -> classBuilder.withSuperclass(elem.innerClass().asSymbol())));

		consumeWriter.accept(newBytes);
	}

	/**
	 * Modify section of the compiled class file based on mapped changes.
	 *
	 * @param mappedChanges the mapped changes
	 * @param cacheModel    the cache model representing the current state
	 */
	@Override
	public void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges,
			CacheModel cacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying binary entries...");
		mappedChanges.entrySet().forEach(entry -> {
			Supplier<Stream<Map.Entry<Integer, CachePropertiesData>>> keys = () -> entry.getValue().entrySet().stream();

			switch (entry.getKey()) {
				case INSERT -> insertFieldSection(keys.get().toList(), cacheModel);
				case DELETE -> keys.get().map(entries -> {
					ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm
							&& fm.fieldName().stringValue().equals(parseFieldName(entries.getValue().propKey())));
					return cf.transformClass(cm, ct);
				}).reduce((_, arr) -> arr).ifPresent(arr -> consumeWriter.accept(arr));
			}
		});
	}

	/**
	 * Insert class section to the compiled class file. Performs operation on a
	 * single path.
	 *
	 * @param <T>  the generic type to be associated with the argument (String or
	 *             Path)
	 * @param path the properties file path
	 */
	public <T> void insertClassSection(T path) {
		insertClassSection(List.of(Path.of(path.toString())));
	}

	/**
	 * Erase class section to the compiled class file. Performs operation on a
	 * single path.
	 *
	 * @param currentCacheModel the current cache model to process
	 */
	public void eraseClassSection(CacheModel currentCacheModel) {
		eraseClassSection(List.of(currentCacheModel));
	}

	/**
	 * Builds the inner field annotation.
	 *
	 * @param key  the key representing the "key" method in the annotation
	 * @param hash the hash representing the "hash" method in the annotation
	 * @return the builded runtime visible annotations attribute
	 */
	private RuntimeVisibleAnnotationsAttribute buildInnerFieldAnnotation(String key, Integer hash) {
		final Class<?> annotationClass = GeneratedInnerField.class;
		String keyMethodName = null;
		String hashMethodName = null;

		try {
			keyMethodName = annotationClass.getDeclaredMethod("key").getName();
			hashMethodName = annotationClass.getDeclaredMethod("hash").getName();
		} catch (NoSuchMethodException e) {
			Utils.handleException(e);
		}

		return RuntimeVisibleAnnotationsAttribute.of(Annotation.of(annotationClass.describeConstable().orElseThrow(),
				List.of(AnnotationElement.ofString(keyMethodName, key),
						AnnotationElement.ofInt(hashMethodName, hash))));
	}

	/**
	 * Builds the inner class annotation.
	 *
	 * @param <T>      the generic type to be associated with the parameter
	 *                 pathToWrite (String or Path)
	 * @param filePath the file path representing the "filePath" method in the
	 *                 annotation
	 * @param javaType the java type representing the "javaType" method in the
	 *                 annotation
	 * @param hash     the hash representing the "hash" method in the annotation
	 * @return the builded runtime visible annotations attribute
	 */
	private <T> RuntimeVisibleAnnotationsAttribute buildInnerClassAnnotation(T filePath, Class<?> javaType,
			Integer hash) {
		final Class<?> annotationClass = GeneratedInnerStaticClass.class;
		String filePathMethodName = null;
		String javaTypeMethodName = null;
		String hashMethodName = null;

		try {
			filePathMethodName = annotationClass.getDeclaredMethod("filePath").getName();
			javaTypeMethodName = annotationClass.getDeclaredMethod("javaType").getName();
			hashMethodName = annotationClass.getDeclaredMethod("hash").getName();
		} catch (NoSuchMethodException e) {
			Utils.handleException(e);
		}

		return RuntimeVisibleAnnotationsAttribute.of(Annotation.of(annotationClass.describeConstable().orElseThrow(),
				List.of(AnnotationElement.ofString(filePathMethodName, filePath.toString()),
						AnnotationElement.ofClass(javaTypeMethodName, javaType.describeConstable().orElseThrow()),
						AnnotationElement.ofInt(hashMethodName, hash))));
	}

	/**
	 * Handles controlled insertion of internal fields into the enclosing static
	 * inner class
	 *
	 * @param entryList the entry list of mapped changes
	 * @param model     the cache model representing the current state
	 */
	private void insertFieldSection(List<Entry<Integer, CachePropertiesData>> entryList, CacheModel model) {
		try {
			Path classPath = Utils.convertSourcePathToClassPath(model.filePath);
			ClassModel clsModel = cf.parse(classPath);

			biConsumeWriter.accept(classPath, cf.build(clsModel.thisClass().asSymbol(), classBuilder -> {
				clsModel.elementStream().forEach(classBuilder::with);

				entryList.forEach(entry -> {
					Integer hash = entry.getKey();
					CachePropertiesData cachedFieldValues = entry.getValue();

					classBuilder.withField(parseFieldName(cachedFieldValues.propKey()),
							model.parseJavaType().describeConstable().get(),
							fieldBuilder -> fieldBuilder
									.with(buildInnerFieldAnnotation(cachedFieldValues.propKey(), hash))
									.with(ConstantValueAttribute.of(cachedFieldValues.rawPropValue().toString()))
									.withFlags(OutterClassModel.getModel(model.filePath).byteCodeModifiers()));
				});
			}));
		} catch (IOException | ClassNotFoundException e) {
			Utils.handleException(e);
		}
	}
}
