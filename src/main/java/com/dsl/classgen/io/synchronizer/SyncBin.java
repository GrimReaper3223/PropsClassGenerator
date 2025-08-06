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
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.Parsers;
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
		// document why this method is empty
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

											classBuilder.withField(parseFieldName(cachedFieldValues.propKey()),
													cacheModel.parseJavaType().describeConstable().get(),
													fieldBuilder -> fieldBuilder
															.with(buildAnnotation(cachedFieldValues.propKey(),
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
	
	private RuntimeVisibleAnnotationsAttribute buildAnnotation(String key, Integer hash) {
		String keyMethodName = null;
		String hashMethodName = null;
		
		try {
			keyMethodName = GeneratedInnerField.class.getDeclaredMethod("key").getName();
			hashMethodName = GeneratedInnerField.class.getDeclaredMethod("hash").getName();
		} catch (NoSuchMethodException e) {
			Utils.logException(e);
		}
		
		return RuntimeVisibleAnnotationsAttribute.of(
				Annotation.of(GeneratedInnerField.class.describeConstable().orElseThrow(), List.of(
							AnnotationElement.ofString(keyMethodName, key),
							AnnotationElement.ofInt(hashMethodName, hash)
						))
				);
	}
}
