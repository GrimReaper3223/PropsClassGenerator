package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public final class SyncBin extends SupportProvider implements SyncOperations {

	private final ClassFile cf = ClassFile.of();
	private ClassModel cm;
	
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
		
		Writer.write(pathsCtx.getOutputClassFilePath(), newBytes);
	}
	
	@Override
	public void modifySection(ModelMapper<Map<Integer, CachePropertiesData>> mappedChanges, CacheModel cacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying binary entries...");
		mappedChanges.modelMap.entrySet().forEach(entry -> {
			Supplier<Stream<CachePropertiesData>> keys = () -> entry.getValue().values().stream();
			ByteBuffer bb = ByteBuffer.allocate(Short.MAX_VALUE);
			
			switch(entry.getKey()) {
				case INSERT:
					
					break;
					
				case DELETE:
					keys.get()
						.forEach(key -> {
							ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm && fm.fieldName().stringValue().toLowerCase().replace("_", ".").contains(key.propKey()));
							bb.put(cf.transformClass(cm, ct));
						});
					break;
			}
			
			Writer.write(pathsCtx.getOutputClassFilePath(), bb.array());
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
	
//	private RuntimeVisibleAnnotationsAttribute buildAnnotation(String key, Integer hash) {
//		String hashFieldName = null;
//		String keyFieldName = null;
//		
//		try {
//			hashFieldName = GeneratedInnerField.class.getDeclaredField("hash").getName();
//			keyFieldName = GeneratedInnerField.class.getDeclaredField("key").getName();
//		} catch (NoSuchFieldException e) {
//			Utils.logException(e);
//		}
//		
//		return RuntimeVisibleAnnotationsAttribute.of(
//				Annotation.of(GeneratedInnerField.class.describeConstable().orElseThrow(), List.of(
//						AnnotationElement.ofInt(hashFieldName, hash),
//							AnnotationElement.ofString(keyFieldName, key)
//						))
//				);
//	}
}
