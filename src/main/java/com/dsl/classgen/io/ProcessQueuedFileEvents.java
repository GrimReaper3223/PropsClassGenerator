package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.sync_refs.SyncSource;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public class ProcessQueuedFileEvents {

	private static final Logger LOGGER = LogManager.getLogger(ProcessQueuedFileEvents.class);
	private static final Thread eventProcessorThread = new Thread(ProcessQueuedFileEvents::processChanges);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	
	private static SyncSource syncSource = new SyncSource();
	
	private ProcessQueuedFileEvents() {}
	
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
			pathsCtx.getAllChangedEntriesFromQueue()
					.stream()
					.collect(Collectors.groupingBy(entry -> (WatchEvent.Kind<?>) entry.getValue(), 
					  Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
					.entrySet()
					.stream()
					.forEach(entry -> {
						Stream<Path> pipeline = entry.getValue().stream();
						switch(entry.getKey().name()) {
							case "ENTRY_CREATE" -> createEntry(pipeline);
							case "ENTRY_DELETE" -> deleteEntry(pipeline);
							case "ENTRY_MODIFY" -> modifyEntry(pipeline);
							default -> {}
						}
					});
			
		}
		
		eventProcessorThread.interrupt();
	}
	
	// adiciona um novo arquivo a lista de arquivos e, se for um diretorio, registra o novo diretorio como chave
	// ao adicionar um novo arquivo, deve-se adicionar a classe interna correspondente no source, junto com todos os seus campos
	private static void createEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			 if(Files.isDirectory(path)) {
				 WatchServiceImpl.registerNewDir(path);
			 } else {
				 pathsCtx.addFileToList(path);
			 }
			 CacheManager.processCache();
		 });
	}
	
	// remove um arquivo da lista de arquivos e, se for um diretorio, desregistra a chave do diretorio existente
	// ao remover o arquivo, deve-se remover a classe interna correspondente no source.
	private static void deleteEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			// se for removido um diretorio, devemos atualizar o source removendo toda a classe interna estatica e seus membros
			if(Files.isDirectory(path)) {
				LOGGER.warn("Existing directory deleted. Deleting cache and reprocessing source file entries...");
				
				try(Stream<Path> files = Files.walk(path)) {
					files.filter(Files::isRegularFile)
						 .filter(Utils::isPropertiesFile)
						 .map(Utils::resolveJsonFilePath)
						 .forEach(jsonPath -> syncSource.eraseClassSection(CacheManager.removeElementFromCacheModelMap(jsonPath)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			// se for removido somente o arquivo, devemos atualizar o source removendo somente a propriedade de uma determinada classe interna estatica
			// por fim, devemos deletar o arquivo de cache do sistema de arquivos e o cache do mapa de cache carregado
			} else if(Utils.isPropertiesFile(path)) {
				syncSource.eraseClassSection(CacheManager.removeElementFromCacheModelMap(Utils.resolveJsonFilePath(path)));
			}
		});
	}
	
	// modifica somente os dados dentro do arquivo. Toda operacao de modificacao opera em cima de alguma alteracao no conteudo do arquivo
	// ao modificar somente uma secao do arquivo, deve-se utilizar do hash do arquivo modificado para achar a classe interna
	// encontrando a classe interna, devemos verificar seu hash em cache com o novo hash feito
	// apos a identificacao, devemos atualizar somente a secao do campo que corresponder ao hash e a chave presente no map daquele CacheModel
	private static void modifyEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			if(Files.isDirectory(path)) {
				LOGGER.warn("Existing directory modified. Modifying cache and reprocessing files...");
				// deve verificar o cache e modificar o conteudo do diretorio alterado
			}
		});
	}
}
