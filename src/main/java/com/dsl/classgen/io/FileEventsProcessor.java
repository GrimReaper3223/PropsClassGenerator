package com.dsl.classgen.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.synchronizer.ModelMapper;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncOptions;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

/**
 * The Class FileEventsProcessor.
 */
public final class FileEventsProcessor extends SupportProvider {

	private static final Thread eventProcessorThread = new Thread(FileEventsProcessor::processChanges);
	private FileEventsProcessor() {}

	/**
	 * Initialize thread.
	 */
	public static void initialize() {
		if (eventProcessorThread.isAlive()) {
			eventProcessorThread.interrupt();
			if(!eventProcessorThread.isInterrupted()) {
				initialize();
			}
		}
		eventProcessorThread.setDaemon(false);
		eventProcessorThread.setName("File Event Processor - Thread");
		eventProcessorThread.start();
	}

	/**
	 * Process file changes detected.
	 */
	private static void processChanges() {
		try {
			while (WatchServiceImpl.getThread().isAlive()) {
				if (!pathsCtx.isEmptyChangedFilesMap() && pathsCtx.getLocker().tryLock()) {
					caller(pathsCtx.getMappedChangedFiles());
					pathsCtx.clearMapOfChanges();
					pathsCtx.getLocker().unlock();
				}
				Thread.onSpinWait();
			}
			LOGGER.error("'{}' was interrupted. Finishing '{}'...", WatchServiceImpl.getThread().getName(), eventProcessorThread.getName());
			eventProcessorThread.interrupt();
		} catch (Exception e) {
			LOGGER.error("An error occurred in the thread '{}'.", eventProcessorThread.getName());
			Utils.handleException(e);
		}
	}

	/**
	 * Provides a manual call to the change processor for other areas of the system
	 */
	public static void caller(Set<Entry<Kind<Path>, Set<Path>>> mappedChangedFiles) {
		mappedChangedFiles.stream().forEach(entry -> {
			if(!entry.getValue().isEmpty()) {
				Kind<Path> kind = entry.getKey();
				switch (kind) {
					case Kind<Path> _ when kind.equals(ENTRY_CREATE) -> createSection(entry.getValue());
					case Kind<Path> _ when kind.equals(ENTRY_DELETE) -> deleteSection(entry.getValue());
					case Kind<Path> _ when kind.equals(ENTRY_MODIFY) -> modifySection(entry.getValue());
					default -> throw new IllegalArgumentException("** BUG ** Unexpected values: " + entry.getValue().toString());
				}
			}
		});
	}

	public static void caller(Kind<Path> kind, Path path) {
		caller(Set.of(Map.entry(kind, Set.of(path))));
	}

	/**
	 * Creates the section.
	 *
	 * @param path the new file path
	 */
	private static void createSection(Set<Path> fileList) {
		new SyncSource().insertClassSection(fileList);
		new SyncBin().insertClassSection(fileList);
	}

	/**
	 * Delete section.
	 *
	 * @param path the file path that should be deleted
	 */
	private static void deleteSection(Set<Path> fileSet) {
		LOGGER.warn("Existing file(s) deleted. Deleting cache and reprocessing source file entries...");
		Set<CacheModel> modelSet = fileSet.stream().map(CacheManager::removeElementFromCacheModelMap).collect(Collectors.toSet());

		new SyncSource().eraseClassSection(modelSet);
		new SyncBin().eraseClassSection(modelSet);
	}

	/**
	 * Modify section.
	 *
	 * @param path the file path that was modified
	 */
	private static void modifySection(Set<Path> filePath) {
		List<CacheModel> currentCacheModelList = filePath.stream().map(CacheManager::getCacheModelFromMap).filter(Objects::nonNull).toList();
		if (currentCacheModelList.isEmpty()) {
			LOGGER.error("No models were found loaded in the cache.");
			return;
		}

		final class ExtendedCacheModel extends CacheModel {

			private static final long serialVersionUID = 1L;

			public ExtendedCacheModel(InnerStaticClassModel model) {
				super(model);
			}

			public boolean checkHash(CacheModel currentCacheModel) {
				return this.fileHash == currentCacheModel.fileHash;
			}
		}

		currentCacheModelList.forEach(currentCacheModel -> {
			Path path = Path.of(currentCacheModel.filePath);
			Reader.read(path);
			InnerStaticClassModel newModel = InnerStaticClassModel.initInstance(path);
			ExtendedCacheModel newCacheModel = new ExtendedCacheModel(newModel);
			OutterClassModel.computeModelToMap(newModel);

			boolean isHashEquals = newCacheModel.checkHash(currentCacheModel);

			if(!isHashEquals) {
				Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges = new ModelMapper<>()
						.mapper(currentCacheModel.entries, newCacheModel.entries);

				new SyncSource().modifySection(mappedChanges, currentCacheModel);
				new SyncBin().modifySection(mappedChanges, newCacheModel);
			}
		});
		CacheManager.processCache();
	}

	public static Thread getThread() {
		return eventProcessorThread;
	}
}
