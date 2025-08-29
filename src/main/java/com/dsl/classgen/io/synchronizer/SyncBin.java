package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
	 * @param pathSet the path list to process
	 */
	@Override
	public void insertClassSection(Set<Path> pathSet) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new compiled data entries...");
		List<InnerStaticClassModel> classModelList = pathSet.stream().map(OutterClassModel::getModel).toList();
		classModelList.stream()
				.map(classModel -> createInnerClassFields(createClassSection(classModel), classModel))
				.reduce((_, arr) -> arr)
				.ifPresent(consumeWriter::accept);
	}

	/**
	 * Erase class section from the compiled class file. Perform operation in a
	 * batch manner, processing a list of cache models
	 *
	 * @param currentCacheModelSet the current cache model list to process
	 */

	@Override
	public void eraseClassSection(Set<CacheModel> currentCacheModelSet) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing compiled class section...");

		// lista de nomes de arquivos convertidos para o formato de classpath
		currentCacheModelSet.stream().<Path>mapMulti((model, consumer) -> {
			try {
				var path = Utils.convertSourcePathToClassPath(model.filePath);
				consumer.accept(path);
			} catch (ClassNotFoundException _) {
				LOGGER.error("Class not found for the given source path: {}.", model.filePath);
				LOGGER.log(LogLevels.NOTICE.getLevel(), "Checking next entry...");
			}
		}).collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
			// gera e escreve o byte array da classe externa atualizada
			if(!list.isEmpty()) {
				consumeWriter.accept(removeSomeExistingClassSections(list));
			} else {
				LOGGER.error("No bytecode class deletion performed, because the list that should have the reference files is empty.");
			}
			return null;
		}));
	}

	/**
	 * Modify section of the compiled class file based on mapped changes.
	 *
	 * @param mappedChanges the mapped changes
	 * @param cacheModel    the cache model representing the current state
	 */
	@Override
	public void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges, CacheModel cacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying binary entries...");
		mappedChanges.entrySet().forEach(entry -> {
			Supplier<Stream<Map.Entry<Integer, CachePropertiesData>>> entries = () -> entry.getValue().entrySet().stream();

			switch (entry.getKey()) {
				case INSERT -> createFieldSectionForExistingClass(entries.get().toList(), cacheModel);
				case DELETE -> removeFieldSectionForExistingClass(entries.get());
			}
		});
	}

	//
	public void insertModuleToOutterClass(ModuleAttribute attr) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Adding module to outter class...");
		consumeWriter.accept(cf.build(cm.thisClass().asSymbol(), handler -> {
			cm.elementStream().forEach(handler::accept);
			handler.accept(attr);
		}));
	}

	/**
	 * Builds the inner field annotation.
	 *
	 * @param key  the key representing the "key" method in the annotation
	 * @param hash the hash representing the "hash" method in the annotation
	 * @return the builded runtime visible annotations attribute
	 */
	private RuntimeVisibleAnnotationsAttribute buildInnerFieldAnnotation(String key, int hash) {
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
	private <T> RuntimeVisibleAnnotationsAttribute buildInnerClassAnnotation(T filePath, Class<?> javaType, int hash) {
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

	private ClassModel createClassSection(InnerStaticClassModel classModel) {
		// cria a classe interna
		Consumer<ClassBuilder> classDeclHandler = cb -> cb
				.withSuperclass(cm.thisClass().asSymbol()).withFlags(classModel.byteCodeModifiers())
				.with(buildInnerClassAnnotation(classModel.annotationMetadata().filePath(),
						classModel.annotationMetadata().javaType(), classModel.annotationMetadata().hash()))
				.withMethod(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PRIVATE,
						mtdBuilder -> mtdBuilder.with(RuntimeVisibleAnnotationsAttribute.of(Annotation
								.of(GeneratedPrivateConstructor.class.describeConstable().orElseThrow()))));

		return cf.parse(cf.build(ClassDesc.of(classModel.className()), classDeclHandler));
	}

	private byte[] createInnerClassFields(ClassModel innerClassModel, InnerStaticClassModel	classModel) {
		Consumer<ClassBuilder> fieldDeclHandler = cb -> {
			// preserva os elementos originais da classe interna
			innerClassModel.elementStream().forEach(cb::with);

			// adiciona os novos campos
			classModel.fieldModelList()
					.forEach(fieldModel -> cb.withField(fieldModel.fieldName(),
							fieldModel.fieldType().describeConstable().orElseThrow(),
							fieldBuilder -> fieldBuilder
									.with(buildInnerFieldAnnotation(fieldModel.annotationMetadata().key(),
											fieldModel.annotationMetadata().hash()))
									.with(ConstantValueAttribute.of(fieldModel.rawFieldValue().toString()))
									.withFlags(fieldModel.byteCodeModifiers())));
		};

		return ByteBuffer.wrap(cf.build(innerClassModel.thisClass().asSymbol(), fieldDeclHandler)).array();
	}

	/**
	 * Handles controlled insertion of internal fields into the enclosing static
	 * inner class
	 *
	 * @param entryList the entry list of mapped changes
	 * @param model     the cache model representing the current state
	 */
	private void createFieldSectionForExistingClass(List<Entry<Integer, CachePropertiesData>> entryList, CacheModel model) {
		try {
			Path classPath = Utils.convertSourcePathToClassPath(model.filePath);
			ClassModel clsModel = cf.parse(classPath);

			biConsumeWriter.accept(classPath, cf.build(clsModel.thisClass().asSymbol(), classBuilder -> {
				clsModel.elementStream().forEach(classBuilder::with);

				entryList.forEach(entry -> {
					int hash = entry.getKey().intValue();
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

	private void removeFieldSectionForExistingClass(Stream<Map.Entry<Integer, CachePropertiesData>> entries) {
		entries.map(entry -> {
			ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm
					&& fm.fieldName().stringValue().equals(parseFieldName(entry.getValue().propKey())));
			return cf.transformClass(cm, ct);
		}).reduce((_, arr) -> arr)
		.ifPresent(arr -> consumeWriter.accept(arr));
	}

	private byte[] removeSomeExistingClassSections(List<Path> fileNameList) {
		// handler que remove as classes internas que não estão mais presentes na lista
		Consumer<ClassBuilder> classEraserHandler = cb -> cm.attributes().stream()
				.filter(InnerClassesAttribute.class::isInstance)
				.map(InnerClassesAttribute.class::cast)
				.flatMap(attr -> attr.classes().stream())
				.filter(elem -> !fileNameList.contains(Path.of(elem.innerClass().name().stringValue())))
				.map(InnerClassesAttribute::of)
				.forEach(cb::with);
		return cf.build(cm.thisClass().asSymbol(), classEraserHandler);
	}
}
