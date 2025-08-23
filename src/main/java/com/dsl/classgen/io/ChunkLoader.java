package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public final class ChunkLoader extends SupportProvider {

	public static void loadChunks() {
		LOGGER.info("Loading chunks...");
		CountDownLatch latch = new CountDownLatch(2);
		var fileList = pathsCtx.getFileList();

		// load class models
		new Thread(() -> {
			fileList.forEach(InnerStaticClassModel::initInstance);
			latch.countDown();
		}).start();

		// load cache
		new Thread(() -> {
			try {
				if(Files.exists(pathsCtx.getCacheDir()) && Files.size(pathsCtx.getCacheDir()) > 0
						&& flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource()) {
					CacheManager.loadCache();
				}
			} catch (IOException e) {
				Utils.handleException(e);
			} finally {
				latch.countDown();
			}
		}).start();

		try {
			latch.await();
			LOGGER.log(LogLevels.SUCCESS.getLevel(), "Chunks loaded successfully.");
			LOGGER.info("Checking data integrity...");
			fileList.forEach(CacheManager::testFileIntegrity);
			resync();
		} catch (InterruptedException e) {
			Utils.handleException(e);
		}
	}

	private static void resync() {
		if(flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource() && flagsCtx.getIsExistsCompiledPJavaClass()) {
			LOGGER.warn("There is already a generated structure.");
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Looking for changes...");

			/*
			 * FIX: Arquivos de cache nao sao excluidos corretamente
			 */
			List<CacheModel> outdatedCache = filterDeletedFiles();

			if(!outdatedCache.isEmpty()) {
				LOGGER.warn("Changes detected. Synchronizing entities...");
				new SyncSource().eraseClassSection(outdatedCache);
				new SyncBin().eraseClassSection(outdatedCache);
			}

			if(CacheManager.hasCacheToWrite()) {
				writeNewCacheIfExists();
			}

			LOGGER.warn("All is up to date.");
		}
	}

	private static List<CacheModel> filterDeletedFiles() {
		Set<String> innerClassesModelKeySet = OutterClassModel.getMapModel().keySet();
		Set<String> cacheModelKeySet = CacheManager.getCacheModelMapEntries()
				.stream()
				.map(entry -> entry.getValue().filePath)
				.collect(Collectors.toSet());

		if(cacheModelKeySet.removeAll(innerClassesModelKeySet) && cacheModelKeySet.isEmpty()) {
			return Collections.emptyList();
		}

		return cacheModelKeySet.stream()
				.map(CacheManager::removeElementFromCacheModelMap)
				.filter(Objects::nonNull)
				.toList();
	}

	private static void writeNewCacheIfExists() {
		LOGGER.warn("New cache to write. Synchronizing entities...");
		CacheManager.getQueuedCacheFiles(false)
					.stream()
					.forEach(path -> {
						WatchEvent.Kind<Path> event = Files.exists(Utils.toJsonFilePath(path)) ? StandardWatchEventKinds.ENTRY_MODIFY : StandardWatchEventKinds.ENTRY_CREATE;
						FileEventsProcessor.caller(event, path);
					});
	}
}
