package com.dsl.classgen.io.synchronizer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;

public final class SyncSource extends SupportProvider implements SyncOperations {

	private final Supplier<StringBuilder> sbSupplier = () -> Reader.readSource(pathsCtx.getExistingPJavaGeneratedSourcePath());
	
	@Override
	public void insertClassSection(List<Path> pathList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new data entries...");
		StringBuilder sb = sbSupplier.get();
		String pattern = "// CLASS-FILE-START";
		int propsFileStartIndex = sb.indexOf(pattern) + pattern.length() + 1;
		
		pathList.forEach(path -> {
			InnerStaticClassModel model = InnerStaticClassModel.initInstance(path);
			OutterClassModel.computeClassModelToMap(model);
			CacheManager.computeCacheModelToMap(path, new CacheModel(model));
			String generatedClass = '\t' + innerClassGen.generateInnerStaticClass(model) + '\n';
			
			sb.insert(propsFileStartIndex, generatedClass);
		});
		
		invokeWriterCondition(sb);
	}
	
	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing class section...");
		List<String> lookupPatternList = AnnotationProcessor.processClassAnnotations(currentCacheModelList);
		StringBuilder sb = sbSupplier.get();
		
		deleteSourceContentUsingDelimiters(sb, lookupPatternList, 2);
		invokeWriterCondition(sb);
	}
	
	@Override
	public void modifySection(ModelMapper<Map<Integer, CachePropertiesData>> mappedChanges, CacheModel currentCacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying source entries...");
		
		StringBuilder sb = sbSupplier.get();
		
		mappedChanges.modelMap.entrySet().forEach(entry -> {
			Supplier<Stream<CachePropertiesData>> streamEntry = () -> entry.getValue().values().stream();
			
			switch(entry.getKey()) {
				case INSERT: 
					streamEntry.get()
							   .map(cachePropData -> {
								   var model = OutterClassModel.getModel(currentCacheModel.filePath).insertNewModel(cachePropData.propKey(), cachePropData.propValue(), currentCacheModel.javaType);
								   return "\t\t" + innerFieldGen.generateInnerField(model) + "\n";
							   })
						   	   .forEach(val -> {
						   		   String pattern = String.format("// PROPS-CONTENT-START: %s", pathsCtx.getPropertiesFileName());
						   		   sb.insert(sb.indexOf(pattern) + pattern.length() + 1, val);
						   	   });
					break;
					
				case DELETE:
					streamEntry.get()
							   .map(element -> AnnotationProcessor.processFieldAnnotations(currentCacheModel.fileHash, Objects.hash(element.propKey(), currentCacheModel.javaType.cast(element.propValue()))))
							   .forEach(lookupPattern -> deleteSourceContentUsingDelimiters(sb, lookupPattern, 3));
					break;
			}
		});
		invokeWriterCondition(sb);
		CacheManager.processCache();
	}

	private void deleteSourceContentUsingDelimiters(StringBuilder sb, List<String> lookupPatternList, int endPatternFullIndexIncrement) {
		lookupPatternList.stream()
						 .forEach(pattern -> {
							 if(pattern != null) {
								String sourceStartHint = pattern.substring(0, pattern.indexOf('@'));
								String sourceEndHint = pattern.substring(pattern.indexOf('@') + 1);
								int endPatternFullIndex = sourceEndHint.length() + endPatternFullIndexIncrement;
								
								sb.delete(sb.indexOf(sourceStartHint) - 1, sb.indexOf(sourceEndHint) + endPatternFullIndex);
							 } else {
								 LOGGER.error("Source element cannot be found.");
							 }
						 });
	}

	private void invokeWriterCondition(StringBuilder sb) {
		if(!sb.equals(sbSupplier.get())) {
			Writer.write(pathsCtx.getExistingPJavaGeneratedSourcePath(), sb.toString());
		}
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
	
	private void deleteSourceContentUsingDelimiters(StringBuilder sb, String lookupPattern, int endPatternFullIndexIncrement) {
		deleteSourceContentUsingDelimiters(sb, List.of(lookupPattern), endPatternFullIndexIncrement);
	}
}
