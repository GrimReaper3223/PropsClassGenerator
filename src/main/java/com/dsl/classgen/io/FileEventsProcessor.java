package com.dsl.classgen.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.synchronizer.ModelMapper;
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
				
				caller(kind, entry.getKey());
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
	
	public static final void caller(WatchEvent.Kind<Path> kind, Path path) {
		switch(kind) {
			case Kind<Path> _ when kind.equals(ENTRY_CREATE) -> createSection(path);
			case Kind<Path> _ when kind.equals(ENTRY_DELETE) -> deleteSection(path);
			case Kind<Path> _ when kind.equals(ENTRY_MODIFY) -> modifySection(path);
			default -> throw new IllegalArgumentException("*** BUG *** - Unexpected value: " + path);
		}
	}
	
	private static void createSection(Path path) {
		if(Files.isDirectory(path)) {
			try(Stream<Path> files = streamFilterCreator.apply(path)) {
				var fileList = files.toList();
				syncSource.insertClassSection(fileList);
				syncBin.insertClassSection(fileList);
			} 
		} else {
			syncSource.insertClassSection(path);
			syncBin.insertClassSection(path);
		}
	}
	
	private static void deleteSection(Path path) {
		List<CacheModel> list = new ArrayList<>();
			
		if(Files.isDirectory(path)) {
			LOGGER.warn("Existing directory deleted. Deleting cache and reprocessing source file entries...");
			
			try(Stream<Path> files = streamFilterCreator.apply(path)) {
				list.addAll(files.map(CacheManager::removeElementFromCacheModelMap)
								 .toList());
			}
		} else if(Utils.isPropertiesFile(path)) {
			LOGGER.warn("Existing file deleted. Deleting cache and reprocessing source file entries...");
			list.add(CacheManager.removeElementFromCacheModelMap(path));
		}
			
		syncSource.eraseClassSection(list);
		syncBin.eraseClassSection(list);
	}
	
	private static void modifySection(Path path) {
		CacheModel currentCacheModel = CacheManager.getElementFromCacheModelMap(path);
		if(currentCacheModel == null) {
			return;
		}
		
		final class ExtendedCacheModel extends CacheModel {

			private static final long serialVersionUID = 1L;

			public ExtendedCacheModel(Path filePath, Properties props) {
				super(filePath, props);
			}
			
			public boolean checkHash(CacheModel currentCacheModel) {
				return this.fileHash == currentCacheModel.fileHash;
			}
			
			public boolean checkPropertyMap(CacheModel currentCacheModel) {
				return this.hashTableMap.entrySet().equals(currentCacheModel.hashTableMap.entrySet());
			}
		}
		
		Path filePath = Path.of(currentCacheModel.filePath);
		Reader.read(filePath);
		ExtendedCacheModel newCacheModel = new ExtendedCacheModel(filePath, generalCtx.getProps());
		
		boolean isHashEquals = newCacheModel.checkHash(currentCacheModel);
		boolean isPropertyMapEntriesEquals = newCacheModel.checkPropertyMap(currentCacheModel);
		
		if(!isHashEquals) {
			if(isPropertyMapEntriesEquals) {
				// atualiza somente o arquivo de classe interna estatica quando nao ha atualizacoes de campos.
				deleteSection(filePath);
				createSection(filePath);
				
			} else {
				// performa atualizacoes de alta precisao em membros da classe interna estatica
				ModelMapper<Map<String, Integer>> mappedChanges = new ModelMapper<>(currentCacheModel.hashTableMap, newCacheModel.hashTableMap);
				syncSource.modifySection(mappedChanges, currentCacheModel);
				syncBin.modifySection(mappedChanges, currentCacheModel);
			}
		}
	}
}
