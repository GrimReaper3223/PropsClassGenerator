package com.dsl.classgen.io.sync;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;

public final class SyncSource extends SupportProvider implements SyncOperations {

	private final Supplier<StringBuilder> sbSupplier = () -> Reader.readSource(pathsCtx.getExistingPJavaGeneratedSourcePath());
	
	private <T extends Map<String, Integer>> Map<FieldSyncOperationType, Map<String, Integer>> mapper(T mapToProcess, T mapToSearch, FieldSyncOperationType operation) {
		return mapToProcess.entrySet()
						   .stream()
						   .filter(entry -> !mapToSearch.containsValue(entry.getValue()))
						   .flatMap(entry -> Map.of(operation, entry).entrySet().stream())
						   .collect(Collectors.groupingBy(Map.Entry::getKey,
							  Collectors.toMap(entry -> entry.getValue().getKey(), entry -> entry.getValue().getValue())));
	}
	
	@Override
	public void insertClassSection(Path path) {
		final String propsFileEndPattern = "// PROPS-FILE-END";
		StringBuilder sb = sbSupplier.get();
		int propsFileEndIndex = sb.indexOf(propsFileEndPattern) - 1;	// -1 retorna para a linha acima, evitando sobrescrever o padrao no arquivo
		
		Reader.read(path);
		pathsCtx.setInputPropertiesPath(path);
		CacheManager.processCache();
		String generatedClass = '\t' + innerClassGen.generateInnerStaticClass() + '\n';

		sb.insert(propsFileEndIndex, generatedClass);
		Writer.write(sb.toString());
	}

	@Override
	public void eraseClassSection(CacheModel currentCacheModel) {
		String lookupPattern = AnnotationProcessor.processClassAnnotations(currentCacheModel.fileHash);
		
		if(lookupPattern != null) {
			String classSourceStartHint = lookupPattern.substring(0, lookupPattern.indexOf('@'));
			String classSourceEndHint = lookupPattern.substring(lookupPattern.indexOf('@') + 1);
			int endPatternFullIndex = classSourceEndHint.length();
			
			StringBuilder sb = sbSupplier.get();
			sb.delete(sb.indexOf(classSourceStartHint) - 1, sb.indexOf(classSourceEndHint) + endPatternFullIndex + 2);
			
			Writer.write(sb.toString());
		} else {
			LOGGER.warn("Static inner class cannot be found.");
		}
	}

	
	/*
	 * Se existir um valor no oldCache que nao existe no newCache, entao esse valor foi apagado.
	 * Se um valor existir no newCache, mas nao no oldCache, entao esse valor foi adicionado.
	 * 
	 * No primeiro caso, o valor que existe no oldCache deve ser extraido e usado pra apagar uma entrada no codigo fonte.
	 * No segundo caso, o valor existente em newCache deve ser extraido e usado para criar uma entrada no codigo fonte
	 */
	@Override
	public void modifySection(CacheModel currentCacheModel) {
		Path filePath = Path.of(currentCacheModel.filePath);
		Reader.loadPropFile(filePath);
		CacheManager.processCache();
		CacheModel newCacheModel = new CacheModel(filePath, generalCtx.getProps()); // carrega o arquivo de propriedades para, em seguida, criar um outro modelo para comparacao
		
		boolean isHashEquals = currentCacheModel.compareFileHash(newCacheModel);								 // verifica se os hashs do mesmo arquivo sao iguais entre os modelos
		boolean isPropertyMapEntriesEquals = currentCacheModel.comparePropertyMapEntries(newCacheModel);		 // verifica se os elementos dos mapas de propriedades sao iguais entre os modelos
		
		if(!isHashEquals && isPropertyMapEntriesEquals) {
			eraseClassSection(currentCacheModel);
			insertClassSection(filePath);
			
		} else if(!isHashEquals) {
			StringBuilder sb = sbSupplier.get();
			Map<FieldSyncOperationType, Map<String, Integer>> changeMap = mapper(currentCacheModel.hashTableMap, newCacheModel.hashTableMap, FieldSyncOperationType.DELETE);
			changeMap.putAll(mapper(newCacheModel.hashTableMap, currentCacheModel.hashTableMap, FieldSyncOperationType.ADD));
			
			changeMap.entrySet().forEach(entry -> {
				Supplier<Stream<Map.Entry<String, Integer>>> streamEntry = () -> entry.getValue().entrySet().stream();
				
				switch(entry.getKey()) {
					case FieldSyncOperationType.ADD: 
						streamEntry.get()
								   .map(element -> '\t' + innerFieldGen.generateInnerField(element.getKey(), generalCtx.getProps().getProperty(element.getKey()), element.getValue() + '\n'))
							   	   .forEach(val -> sb.insert(sb.indexOf("// PROPS-CONTENT-END") - 1, val));
						break;
						
					case FieldSyncOperationType.DELETE:
						streamEntry.get()
								   .map(element -> AnnotationProcessor.processFieldAnnotations(currentCacheModel.fileHash, element.getValue()))
								   .forEach(lookupPattern -> {
									   if(lookupPattern != null) {
											String classSourceStartHint = lookupPattern.substring(0, lookupPattern.indexOf('@'));
											String classSourceEndHint = lookupPattern.substring(lookupPattern.indexOf('@') + 1);
											int endPatternFullIndex = classSourceEndHint.length();
											
											sb.delete(sb.indexOf(classSourceStartHint) - 1, sb.indexOf(classSourceEndHint) + endPatternFullIndex + 2);
										} else {
											LOGGER.warn("Inner field cannot be found.");
										}
								   });
						break;
				}
				Writer.write(sb.toString());
			});
		}
	}
}
