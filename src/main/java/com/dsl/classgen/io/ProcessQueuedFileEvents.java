package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsl.classgen.io.cache_system.FileCacheSystem;
import com.dsl.classgen.io.file_handler.SyncSource;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public class ProcessQueuedFileEvents {

	private static final Thread eventProcessorThread = new Thread(ProcessQueuedFileEvents::processChanges);
	
	public static void initialize() {
		if(!eventProcessorThread.isAlive()) {
			eventProcessorThread.setDaemon(false);
			eventProcessorThread.setName("File Event Processor - Thread");
			eventProcessorThread.start();
		}
	}
	
	private static void processChanges() {
		while(WatchServiceImpl.isWatchServiceThreadAlive()) {
			if(!Values.checkRemainingChanges()) {
				Thread.onSpinWait();
				continue;
			}
			var eventMap = Values.getAllChangedValuesFromDeque()
								 .stream()
								 .collect(Collectors.groupingBy(entry -> (WatchEvent.Kind<?>) entry.getValue(), 
									  Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
			
			eventMap.entrySet()
					.stream()
					.forEach(entry -> {
						Stream<Path> pipeline = entry.getValue().stream();
						switch(entry.getKey().name()) {
							case "ENTRY_CREATE" -> createEntry(pipeline);
							case "ENTRY_DELETE" -> deleteEntry(pipeline);
							case "ENTRY_MODIFY" -> modifyEntry(pipeline);
						}
					});
		}
		
		eventProcessorThread.interrupt();
	}
	
	// adiciona um novo arquivo a lista de arquivos e, se for um diretorio, registra o novo diretorio como chave
	private static void createEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			 if(Files.isDirectory(path)) {
				 WatchServiceImpl.registerNewDir(path);
			 } else {
				 Values.addFileToList(path);
			 }
			 FileCacheSystem.processCache();
		 });
	}
	
	// remove um arquivo da lista de arquivos e, se for um diretorio, desregistra a chave do diretorio existente
	private static void deleteEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			if(Files.isDirectory(path)) {
				System.out.println("Existing directory deleted. Deleting cache and reprocessing files...");
				
				try(Stream<Path> elements = Files.walk(path)) {
					elements.filter(Files::isRegularFile)
							.filter(Utils::isPropertiesFile)
							.map(propertyFile -> {
								Path jsonFilePath = Utils.resolveJsonFilePath(propertyFile);
								try {
									Files.deleteIfExists(jsonFilePath);
								} catch (IOException e) {
									e.printStackTrace();
								}
								return jsonFilePath;
							})
							.forEach(Values::deleteElementFromHashTableMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// deve verificar o cache e invalidar o diretorio excluido junto ao seu conteudo
			} else {
				// este objeto retornado deve ser usado para remover todas as entradas do .java e do .class
				SyncSource syncSource = new SyncSource(path);
				syncSource.eraseSection();
				
				/*
				 * ...
				 * ...
				 */
				// por fim, devemos deletar o arquivo de cache do sistema de arquivos e o cache do mapa de cache carregado
				Values.deleteElementFromHashTableMap(path);
			}
		});
	}
	
	// modifica somente os dados dentro do arquivo. Toda operacao de modificacao opera em cima de alguma alteracao no conteudo do arquivo
	private static void modifyEntry(Stream<Path> pipeline) {
		pipeline.forEach(path -> {
			if(Files.isDirectory(path)) {
				System.out.println("Existing directory modified. Modifying cache and reprocessing files...");
				// deve verificar o cache e modificar o conteudo do diretorio alterado
			}
		});
	}
}
