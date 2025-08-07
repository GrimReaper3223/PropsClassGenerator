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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.Parsers;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public final class SyncBin extends SupportProvider implements SyncOperations, Parsers {

	private final ClassFile cf = ClassFile.of();
	private ClassModel cm;
	private Consumer<byte[]> consumeWriter = data -> Writer.write(pathsCtx.getOutputClassFilePath(), data);
	private BiConsumer<Path, byte[]> biConsumeWriter = Writer::write;
	
	public SyncBin() {
		try {
			cm = cf.parse(pathsCtx.getOutputClassFilePath());
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	@Override
	public void insertClassSection(List<Path> pathList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new compiled data entries...");
		List<InnerStaticClassModel> classModelList = pathList.stream().map(OutterClassModel::getModel).toList();
		
		classModelList.forEach(classModel -> {
			
			byte[] bytes = cf.build(ClassDesc.of(classModel.className()), classBuilder -> classBuilder
					.withSuperclass(cm.thisClass().asSymbol())
					.withFlags(classModel.byteCodeModifiers())
					.with(buildInnerClassAnnotation(classModel.annotationMetadata().filePath(),
							classModel.annotationMetadata().javaType(), classModel.annotationMetadata().hash()))
					.withMethod(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PRIVATE,
							mtdBuilder -> mtdBuilder.with(RuntimeVisibleAnnotationsAttribute.of(Annotation
									.of(GeneratedPrivateConstructor.class.describeConstable().orElseThrow())))));
			
			ClassModel innerClassFileModel = cf.parse(bytes);
			
			// gera os campos internos
			ByteBuffer bb = ByteBuffer.wrap(cf.build(innerClassFileModel.thisClass().asSymbol(), classBuilder -> {
				innerClassFileModel.elementStream().forEach(classBuilder::with);
				
				classModel.fieldModelList().forEach(fieldModel -> 
					classBuilder.withField(fieldModel.fieldName(), fieldModel.fieldType().describeConstable().orElseThrow(), fieldBuilder -> 
						fieldBuilder.with(buildInnerFieldAnnotation(fieldModel.annotationMetadata().key(), fieldModel.annotationMetadata().hash()))
								.with(ConstantValueAttribute.of(fieldModel.rawFieldValue().toString()))
								.withFlags(fieldModel.byteCodeModifiers())));
			}));
			
			// escreve os novos dados da classe interna na outter class
			consumeWriter.accept(bb.array());
		});
	}

	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing compiled class section...");
		List<Path> fileNameList = currentCacheModelList.stream()
													   .<Path>mapMulti((model, consumer) -> {
														   try {
															   consumer.accept(Utils.convertSourcePathToClassPath(model.filePath));
														   } catch (ClassNotFoundException e) {
															   Utils.logException(e);
														   }
													   })
													   .toList();
		
		byte[] newBytes = cf.build(cm.thisClass().asSymbol(), classBuilder -> 
			cm.attributes()
				.stream()
				.filter(InnerClassesAttribute.class::isInstance)
				.map(InnerClassesAttribute.class::cast)
				.flatMap(attr -> attr.classes().stream())
				.filter(elem -> !fileNameList.contains(Path.of(elem.innerClass().name().stringValue())))
				.forEach(elem -> classBuilder.withSuperclass(elem.innerClass().asSymbol())));
		
		consumeWriter.accept(newBytes);
	}
	
	@Override
	public void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges, CacheModel cacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying binary entries...");
		mappedChanges.entrySet().forEach(entry -> {
			Supplier<Stream<Map.Entry<Integer,CachePropertiesData>>> keys = () -> entry.getValue().entrySet().stream();
			ByteBuffer bb = ByteBuffer.allocate(Short.MAX_VALUE);
			
			switch(entry.getKey()) {
				case INSERT:
					keys.get()
						.forEach(entries -> {
							Integer hashCode = entries.getKey();
							CachePropertiesData cachedFieldValues = entries.getValue();
							
							try {
								Path classPath = Utils.convertSourcePathToClassPath(cacheModel.filePath);
								ClassModel clsModel = cf.parse(classPath);

								biConsumeWriter.accept(classPath,
										cf.build(clsModel.thisClass().asSymbol(), classBuilder -> {
											clsModel.elementStream().forEach(classBuilder::with);

											// TODO: verificar referencias ao parseFieldName
											classBuilder.withField(parseFieldName(cachedFieldValues.propKey()),
													cacheModel.parseJavaType().describeConstable().get(),
													fieldBuilder -> fieldBuilder
															.with(buildInnerFieldAnnotation(cachedFieldValues.propKey(),
																	hashCode))
															.with(ConstantValueAttribute
																	.of(cachedFieldValues.rawPropValue().toString()))
															.withFlags(OutterClassModel.getModel(cacheModel.filePath)
																	.byteCodeModifiers()));
										}));
							} catch (IOException | ClassNotFoundException e) {
								Utils.logException(e);
							}
						});
					break;
					
				case DELETE:
					keys.get()
						.map(Map.Entry::getValue)
						.forEach(key -> {
							ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm && fm.fieldName().stringValue().equals(parseFieldName(key.propKey())));
							bb.put(cf.transformClass(cm, ct));
						});
					consumeWriter.accept(bb.array());
					break;
			}
		});
	}
	
	/*
	 * HELPERS
	 */

	public <T> void insertClassSection(T path) {
		insertClassSection(List.of(Path.of(path.toString())));
	}
	
	public void eraseClassSection(CacheModel currentCacheModel) {
		eraseClassSection(List.of(currentCacheModel));
	}
	
	private RuntimeVisibleAnnotationsAttribute buildInnerFieldAnnotation(String key, Integer hash) {
		final Class<?> annotationClass = GeneratedInnerField.class;
		String keyMethodName = null;
		String hashMethodName = null;
		
		// devemos obter o nome do metodo declarado em cada interface de anotacao
		// isso garante o lancamento de um erro caso o metodo nao exista
		try {
			keyMethodName = annotationClass.getDeclaredMethod("key").getName();
			hashMethodName = annotationClass.getDeclaredMethod("hash").getName();
		} catch (NoSuchMethodException e) {
			Utils.logException(e);
		}
		
		return RuntimeVisibleAnnotationsAttribute.of(
				Annotation.of(annotationClass.describeConstable().orElseThrow(), List.of(
							AnnotationElement.ofString(keyMethodName, key),
							AnnotationElement.ofInt(hashMethodName, hash)))
				);
	}
	
	private <T> RuntimeVisibleAnnotationsAttribute buildInnerClassAnnotation(T filePath, Class<?> javaType, Integer hash) {
		final Class<?> annotationClass = GeneratedInnerStaticClass.class;
		String filePathMethodName = null;
		String javaTypeMethodName = null;
		String hashMethodName = null;
		
		// devemos obter o nome do metodo declarado em cada interface de anotacao
		// isso garante o lancamento de uma excecao caso o metodo nao exista
		try {
			filePathMethodName = annotationClass.getDeclaredMethod("filePath").getName();
			javaTypeMethodName = annotationClass.getDeclaredMethod("javaType").getName();
			hashMethodName = annotationClass.getDeclaredMethod("hash").getName();
		} catch (NoSuchMethodException e) {
			Utils.logException(e);
		}
		
		return RuntimeVisibleAnnotationsAttribute.of(
				Annotation.of(annotationClass.describeConstable().orElseThrow(), List.of(
						AnnotationElement.ofString(filePathMethodName, filePath.toString()),
						AnnotationElement.ofClass(javaTypeMethodName, javaType.describeConstable().orElseThrow()),
						AnnotationElement.ofInt(hashMethodName, hash)))
				);
	}
}
