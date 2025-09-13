package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.dsl.classgen.io.file_manager.Compiler;
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
		var fileSet = pathsCtx.getFileSet();

		// load class models
		Thread t1 = new Thread(() -> {
			fileSet.forEach(filePath -> OutterClassModel.computeModelToMap(InnerStaticClassModel.initInstance(filePath)));
			latch.countDown();
		});

		// load cache
		Thread t2 = new Thread(() -> {
			try {
				if(Files.exists(pathsCtx.getCacheDir()) && Files.size(pathsCtx.getCacheDir()) > 0
						&& flagsCtx.hasSourceStructureGenerated(false)) {
					CacheManager.loadCache();
				}
			} catch (IOException e) {
				Utils.handleException(e);
			} finally {
				latch.countDown();
			}
		});

		t1.setName("ChunkLoader-Thread-1");
		t2.setName("ChunkLoader-Thread-2");
		t1.start();
		t2.start();

		try {
			latch.await();
			// FIX: pattern @|style text|@ not working in log4j2
			LOGGER.log(LogLevels.SUCCESS.getLevel(), "Chunks loaded successfully.");
			resync(fileSet);
			CacheManager.processCache();	// garante que o cache esteja atualizado
		} catch (InterruptedException e) {
			Utils.handleException(e);
		}
	}

	private static void resync(Set<Path> fileSet) {
		LOGGER.info("Checking data integrity...");
		fileSet.forEach(CacheManager::testFileIntegrity);

		if(flagsCtx.hasSourceStructureGenerated(false)) {
			LOGGER.warn("There is already a generated structure.");
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Looking for changes...");

			Set<CacheModel> outdatedCache = filterDeletedFiles();

			if(!outdatedCache.isEmpty()) {
				LOGGER.warn("Changes detected. Synchronizing entities...");
				new SyncSource().eraseClassSection(outdatedCache);
				Compiler.recompile(() -> new SyncBin().eraseClassSection(outdatedCache));
			}

			if(CacheManager.hasCacheToWrite()) {
				writeNewCacheIfExists();
			}

			LOGGER.warn("All is up to date.");
		}
	}

	private static Set<CacheModel> filterDeletedFiles() {
		return CacheManager.getCacheModelMapEntries()
				.stream()
				.map(entry -> entry.getValue().filePath)
				.collect(Collectors.collectingAndThen(Collectors.toSet(), cacheModelKeySet -> {
					if (cacheModelKeySet.removeAll(OutterClassModel.getMapModel().keySet()) && cacheModelKeySet.isEmpty()) {
						return Collections.emptySet();
					}
					return cacheModelKeySet.stream()
							.map(CacheManager::removeElementFromCacheModelMap)
							.filter(Objects::nonNull)
							.collect(Collectors.toSet());
				}));
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
