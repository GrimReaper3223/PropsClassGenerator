package com.dsl.classgen.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class FileEventsProcessor extends SupportProvider {

	private static final Thread eventProcessorThread = new Thread(FileEventsProcessor::processChanges);
	private static SyncSource syncSource = new SyncSource();
	private static SyncBin syncBin = new SyncBin();

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
			try {
				var entry = pathsCtx.getQueuedChangedFilesEntries();
				var kind = entry.getValue();
				
				switch(kind) {
					case Kind<Path> _ when kind.equals(ENTRY_CREATE) -> createSection(List.of(entry.getKey()).stream());
					case Kind<Path> _ when kind.equals(ENTRY_DELETE) -> deleteSection(List.of(entry.getKey()).stream());
					case Kind<Path> _ when kind.equals(ENTRY_MODIFY) -> modifySection(List.of(entry.getKey()).stream());
					default -> throw new IllegalArgumentException("*** BUG *** - Unexpected value: " + entry.getValue());
				}
			} catch (InterruptedException _) {
  		 		if(eventProcessorThread.isInterrupted()) {
  		 			eventProcessorThread.interrupt();
  		 			LOGGER.warn("{} is interrupted. Restarting thread...", eventProcessorThread.getName());
  		 			initialize();
  		 		}
			}
		}
		LOGGER.error("{} was interrupted. Finishing {}...", WatchServiceImpl.getThreadName(), eventProcessorThread.getName());
		eventProcessorThread.interrupt();
	}
	
	private static void createSection(Stream<Path> pipeline) {
		LOGGER.info("Generating new data entries...");
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
	
	private static void deleteSection(Stream<Path> pipeline) {
		List<CacheModel> modelList = pipeline.map(path -> {
			List<CacheModel> list = new ArrayList<>();
			
			if(Files.isDirectory(path)) {
				LOGGER.warn("Existing directory deleted. Deleting cache and reprocessing source file entries...");
				
				try(Stream<Path> files = streamFilterCreator.apply(path)) {
					list.addAll(files.map(element -> CacheManager.removeElementFromCacheModelMap(Utils.resolveJsonFilePath(element)))
								.toList());
				}
			} else if(Utils.isPropertiesFile(path)) {
				LOGGER.warn("Existing file deleted. Deleting cache and reprocessing source file entries...");
				list.add(CacheManager.removeElementFromCacheModelMap(Utils.resolveJsonFilePath(path)));
			}
			
			return list;
		})
		.flatMap(List::stream)
		.toList();
		
		syncSource.eraseClassSection(modelList);
		syncBin.eraseClassSection(modelList);
	}
	
	private static void modifySection(Stream<Path> pipeline) {
		LOGGER.info("Modifying source entries...");
		pipeline.forEach(path -> syncSource.modifySection(CacheManager.getElementFromCacheModelMap(Utils.resolveJsonFilePath(path))));
	}
}
