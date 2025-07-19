package com.dsl.classgen.io.synchronizer;

import static com.dsl.classgen.io.synchronizer.FieldSyncOperation.DELETE;
import static com.dsl.classgen.io.synchronizer.FieldSyncOperation.INSERT;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.utils.Levels;

public final class SyncSource extends SupportProvider implements SyncOperations {

	private final Supplier<StringBuilder> sbSupplier = () -> Reader.readSource(pathsCtx.getExistingPJavaGeneratedSourcePath());
	
	// inicia o fluxo de processamento de um mapa
	private final BiFunction<Map<String, Integer>, 
							 Map<String, Integer>, 
							 Stream<Map.Entry<String, Integer>>> streamMapCreator = 
							 			(map1, map2) -> map1.entrySet()
							 							    .stream()
															.filter(entry -> !map2.containsValue(entry.getValue()));
							 			
	// finaliza o fluxo de processamento e mapa retornando um resultado
    private final BiFunction<Stream<Map.Entry<String, Integer>>, 
    								FieldSyncOperation,
    								Map<FieldSyncOperation, Map<String, Integer>>> streamMapFinisher = 
    									(stream, op) -> stream.flatMap(entry -> Map.of(op, entry)
    																			   .entrySet()
    																			   .stream())
    														  .collect(Collectors.groupingBy(Map.Entry::getKey,
    																  	Collectors.toMap(entry -> entry.getValue().getKey(), 
    																  					 entry -> entry.getValue().getValue())));
	
	private <T extends Map<String, Integer>> Map<FieldSyncOperation, Map<String, Integer>> mapper(T oldMap, T newMap) {
		Map<FieldSyncOperation, Map<String, Integer>> map = new EnumMap<>(FieldSyncOperation.class);

		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(oldMap, newMap), DELETE));
		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(newMap, oldMap), INSERT));
		
		return map;
	}
	
	@Override
	public void insertClassSection(Path path) {
		StringBuilder sb = sbSupplier.get();
		String pattern = "// PROPS-FILE-START";
		int propsFileStartIndex = sb.indexOf(pattern) + pattern.length() + 1;
		
		Reader.read(path);
		pathsCtx.setInputPropertiesPath(path);
		CacheManager.processCache();
		String generatedClass = '\t' + innerClassGen.generateInnerStaticClass() + '\n';

		sb.insert(propsFileStartIndex, generatedClass);
		invokeWriterCondition(sb);
	}

	@Override
	public void eraseClassSection(CacheModel currentCacheModel) {
		LOGGER.log(Levels.NOTICE.getLevel(), "Erasing class section...");
		String lookupPattern = AnnotationProcessor.processClassAnnotations(currentCacheModel.fileHash);
		StringBuilder sb = sbSupplier.get();
		
		deleteSourceContentUsingDelimiters(sb, lookupPattern, 2);
		invokeWriterCondition(sb);
	}
	
	@Override
	public void modifySection(CacheModel currentCacheModel) {
		if(currentCacheModel == null) {
			return;
		}
		
		Path filePath = Path.of(currentCacheModel.filePath);
		Reader.read(filePath);
		CacheModel newCacheModel = new CacheModel(filePath, generalCtx.getProps());
		
		boolean isHashEquals = currentCacheModel.compareFileHash(newCacheModel);
		boolean isPropertyMapEntriesEquals = currentCacheModel.comparePropertyMapEntries(newCacheModel);
		
		if(!isHashEquals) {
			if(isPropertyMapEntriesEquals) {
				eraseClassSection(currentCacheModel);
				insertClassSection(filePath);
				
			} else {
				StringBuilder sb = sbSupplier.get();
				
				mapper(currentCacheModel.hashTableMap, newCacheModel.hashTableMap).entrySet().forEach(entry -> {
					Supplier<Stream<Map.Entry<String, Integer>>> streamEntry = () -> entry.getValue().entrySet().stream();
					
					switch(entry.getKey()) {
						case INSERT: 
							streamEntry.get()
									   .map(element -> "\t\t" + innerFieldGen.generateInnerField(element.getKey(), generalCtx.getProps().getProperty(element.getKey()), element.getValue()) + "\n")
								   	   .forEach(val -> {
								   		   String pattern = String.format("// PROPS-CONTENT-START: %s", pathsCtx.getPropertiesFileName());
								   		   sb.insert(sb.indexOf(pattern) + pattern.length() + 1, val);
								   	   });
							break;
							
						case DELETE:
							streamEntry.get()
									   .map(element -> AnnotationProcessor.processFieldAnnotations(currentCacheModel.fileHash, element.getValue()))
									   .forEach(lookupPattern -> deleteSourceContentUsingDelimiters(sb, lookupPattern, 3));
							break;
						}
				});
				invokeWriterCondition(sb);
			}
			CacheManager.processCache();
			
		} else {
			LOGGER.log(Levels.NOTICE.getLevel(), "Nothing to update.");
		}
	}

	private void deleteSourceContentUsingDelimiters(StringBuilder sb, String lookupPattern, int endPatternFullIndexIncrement) {
		if(lookupPattern != null) {
			String sourceStartHint = lookupPattern.substring(0, lookupPattern.indexOf('@'));
			String sourceEndHint = lookupPattern.substring(lookupPattern.indexOf('@') + 1);
			int endPatternFullIndex = sourceEndHint.length() + endPatternFullIndexIncrement;
			
			sb.delete(sb.indexOf(sourceStartHint) - 1, sb.indexOf(sourceEndHint) + endPatternFullIndex);
		} else {
			LOGGER.error("Source element cannot be found.");
		}
	}
	
	private void invokeWriterCondition(StringBuilder sb) {
		if(!sb.equals(sbSupplier.get())) {
			Writer.write(sb.toString());
		}
	}
}
