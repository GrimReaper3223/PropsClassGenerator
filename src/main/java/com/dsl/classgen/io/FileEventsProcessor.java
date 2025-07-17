package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.sync.SyncSource;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class FileEventsProcessor extends SupportProvider {

	private static final Thread eventProcessorThread = new Thread(FileEventsProcessor::processChanges);
	private static SyncSource syncSource = new SyncSource();

	private static Function<Path, Stream<Path>> streamFilterCreator = path -> {
		Stream<Path> pathStream = null;
		try {
			pathStream = Files.walk(path)
							  .filter(Files::isRegularFile)
							  .filter(Utils::isPropertiesFile);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		
		return pathStream;
	};
	
	private FileEventsProcessor() {}
	
	public static void initialize() {
		if(!eventProcessorThread.isAlive()) {
			eventProcessorThread.setDaemon(false);
			eventProcessorThread.setName("File Event Processor - Thread");
			eventProcessorThread.start();
		}
	}
	
	private static void processChanges() {
		while(WatchServiceImpl.isWatchServiceThreadAlive()) {
			if(!flagsCtx.getHasChangedFilesLeft()) {
				Thread.onSpinWait();
				continue;
			}
			
			pathsCtx.getQueuedChangedFilesEntries()
					.stream()
					.collect(Collectors.groupingBy(entry -> (WatchEvent.Kind<?>) entry.getValue(), 
					  Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
					.entrySet()
					.stream()
					.forEach(entry -> {
						Stream<Path> pipeline = entry.getValue().stream();
						switch(entry.getKey().name()) {
							case "ENTRY_CREATE" -> createSection(pipeline);
							case "ENTRY_DELETE" -> deleteSection(pipeline);
							case "ENTRY_MODIFY" -> modifySection(pipeline);
						}
					});
		}
			
		eventProcessorThread.interrupt();
	}
	
	// adiciona um novo arquivo a lista de arquivos e, se for um diretorio, registra o novo diretorio como chave
	// ao adicionar um novo arquivo, deve-se adicionar a classe interna correspondente no source, junto com todos os seus campos
	private static void createSection(Stream<Path> pipeline) {
		LOGGER.warn("Generating new data entries...");
		pipeline.forEach(path -> {
			if(Files.isDirectory(path)) {
				try(Stream<Path> files = streamFilterCreator.apply(path)) {
					files.forEach(syncSource::insertClassSection);
				} 
			} else {
				syncSource.insertClassSection(path);
			}
		});
	}
	
	// remove um arquivo da lista de arquivos e, se for um diretorio, desregistra a chave do diretorio existente
	// ao remover o arquivo, deve-se remover a classe interna correspondente no source.
	private static void deleteSection(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			// se for removido um diretorio, devemos atualizar o source removendo toda a classe interna estatica e seus membros
			if(Files.isDirectory(path)) {
				LOGGER.warn("Existing directory deleted. Deleting cache and reprocessing source file entries...");
				
				try(Stream<Path> files = streamFilterCreator.apply(path)) {
					files.map(Utils::resolveJsonFilePath)
						 .forEach(jsonPath -> syncSource.eraseClassSection(CacheManager.removeElementFromCacheModelMap(jsonPath)));
				}
			// se for removido somente o arquivo, devemos atualizar o source removendo somente a propriedade de uma determinada classe interna estatica
			// por fim, devemos deletar o arquivo de cache do sistema de arquivos e o cache do mapa de cache carregado
			} else if(Utils.isPropertiesFile(path)) {
				LOGGER.warn("Existing file deleted. Deleting cache and reprocessing source file entries...");
				syncSource.eraseClassSection(CacheManager.removeElementFromCacheModelMap(Utils.resolveJsonFilePath(path)));
			}
		});
	}
	
	// modifica somente os dados dentro do arquivo. Toda operacao de modificacao opera em cima de alguma alteracao no conteudo do arquivo
	// ao modificar somente uma secao do arquivo, deve-se utilizar do hash do arquivo modificado para achar a classe interna
	// encontrando a classe interna, devemos verificar seu hash em cache com o novo hash feito
	// apos a identificacao, devemos atualizar somente a secao do campo que corresponder ao hash e a chave presente no map daquele CacheModel
	// RECEBE SOMENTE ARQUIVOS, POIS OS DIRETORIOS ADICIONAIS SAO NOVAS CHAVES DE MONITORAMENTO
	private static void modifySection(Stream<Path> pipeline) {
		LOGGER.warn("Modifying source entries...");
		pipeline.forEach(path -> syncSource.modifySection(CacheManager.getElementFromCacheModelMap(Utils.resolveJsonFilePath(path))));
	}
}
