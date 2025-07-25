package com.dsl.classgen.io.synchronizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.Objects;

import com.dsl.classgen.io.FileEventsProcessor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.cache_manager.CacheProcessorOption;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;

public final class BootSync extends SupportProvider {

	public void resync() {
		if(flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource() && flagsCtx.getIsExistsCompiledPJavaClass()) {
			SyncSource syncSource = new SyncSource();
			SyncBin syncBin = new SyncBin();
			
			LOGGER.warn("There is already a generated structure.");
			LOGGER.log(Levels.NOTICE.getLevel(), "Checking files...");
			
			CacheManager.processCache(CacheProcessorOption.LOAD);
			
			List<CacheModel> removedModels = removedModels(filterOutOfSyncCache());
			
			if(!removedModels.isEmpty()) {
				LOGGER.warn("Changes detected. Synchronizing entities...");
				syncSource.eraseClassSection(removedModels);
				syncBin.eraseClassSection(removedModels);
			}
			
			writeNewCacheIfExists();
		}
	}
	
	private List<Path> filterOutOfSyncCache() {
		return CacheManager.getCacheModelMapEntries()
				.stream()
				.map(entry -> Path.of(entry.getValue().filePath))
				.filter(elem -> !pathsCtx.getFileList().contains(elem))
				.filter(Objects::nonNull)
				.toList();
	}
	
	private List<CacheModel> removedModels(List<Path> filteredModelList) {
		return filteredModelList.stream()
				 .map(CacheManager::removeElementFromCacheModelMap)
				 .filter(Objects::nonNull)
				 .toList();
	}
	
	private void writeNewCacheIfExists() {
		if(CacheManager.hasCacheToWrite()) {
			LOGGER.warn("New cache to write. Synchronizing entities...");
			CacheManager.getQueuedCacheFiles(false)
						.stream()
						.forEach(path -> {
							WatchEvent.Kind<Path> event = Files.exists(Utils.resolveJsonFilePath(path)) ? StandardWatchEventKinds.ENTRY_MODIFY : StandardWatchEventKinds.ENTRY_CREATE;;
							FileEventsProcessor.caller(event, path);
						});
		}
	}
}
