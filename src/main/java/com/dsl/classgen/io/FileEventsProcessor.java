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

import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.synchronizer.ModelMapper;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncOptions;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.service.WatchServiceImpl;

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
		if (!eventProcessorThread.isAlive()) {
			eventProcessorThread.setDaemon(false);
			eventProcessorThread.setName("File Event Processor - Thread");
			eventProcessorThread.start();
		}
	}

	/**
	 * Process file changes detected.
	 */
	private static void processChanges() {
		while (WatchServiceImpl.isWatchServiceThreadAlive()) {
			if (!pathsCtx.getMappedChangedFiles().isEmpty() && pathsCtx.locker.tryLock()) {
				caller(pathsCtx.getMappedChangedFiles());
				pathsCtx.getMappedChangedFiles().clear();
				pathsCtx.locker.unlock();
			}
			Thread.onSpinWait();
		}
		LOGGER.error("'{}' was interrupted. Finishing '{}'...", WatchServiceImpl.getThreadName(),
				eventProcessorThread.getName());
		eventProcessorThread.interrupt();
	}

	/**
	 * Provides a manual call to the change processor for other areas of the system
	 *
	 * @param <T>  the generic type to be associated with the argument (String or
	 *             Path)
	 */
	public static <T> void caller(Set<Entry<Kind<Path>, List<T>>> mappedChangedFiles) {
		mappedChangedFiles.stream().forEach(entry -> {
			Kind<Path> kind = entry.getKey();
			List<Path> pathList = entry.getValue().stream().map(String::valueOf).map(Path::of).toList();
			switch (kind) {
				case Kind<Path> _ when kind.equals(ENTRY_CREATE) -> createSection(pathList);
				case Kind<Path> _ when kind.equals(ENTRY_DELETE) -> deleteSection(pathList);
				case Kind<Path> _ when kind.equals(ENTRY_MODIFY) -> modifySection(pathList);
				default -> throw new IllegalArgumentException("** BUG ** Unexpected values: " + pathList.toString());
			}
		});
	}

	public static <T> void caller(Kind<Path> kind, T path) {
		caller(Set.of(Map.entry(kind, List.of(path))));
	}

	/**
	 * Creates the section.
	 *
	 * @param path the new file path
	 */
	private static void createSection(List<Path> fileList) {
		new SyncSource().insertClassSection(fileList);
		new SyncBin().insertClassSection(fileList);
	}

	/**
	 * Delete section.
	 *
	 * @param path the file path that should be deleted
	 */
	private static void deleteSection(List<Path> fileList) {
		LOGGER.warn("Existing file(s) deleted. Deleting cache and reprocessing source file entries...");
		List<CacheModel> list = fileList.stream().map(CacheManager::removeElementFromCacheModelMap).toList();

		new SyncSource().eraseClassSection(list);
		new SyncBin().eraseClassSection(list);
	}

	/**
	 * Modify section.
	 *
	 * @param path the file path that was modified
	 */
	private static void modifySection(List<Path> filePath) {
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

			public boolean checkPropertyMap(CacheModel currentCacheModel) {
				return this.entries.equals(currentCacheModel.entries);
			}
		}

		currentCacheModelList.forEach(currentCacheModel -> {
			Path path = Path.of(currentCacheModel.filePath);
			Reader.read(path);
			ExtendedCacheModel newCacheModel = new ExtendedCacheModel(InnerStaticClassModel.initInstance(path));

			boolean isHashEquals = newCacheModel.checkHash(currentCacheModel);
			boolean isPropertyMapEntriesEquals = newCacheModel.checkPropertyMap(currentCacheModel);

			if(!isHashEquals) {
				if (isPropertyMapEntriesEquals) {
					// TODO: Deve implementar a modificacao dos metadados, e nao a regeneracao de uma secao
				} else {
					Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges = new ModelMapper<>()
							.mapper(currentCacheModel.entries, newCacheModel.entries);

					new SyncSource().modifySection(mappedChanges, currentCacheModel);
					new SyncBin().modifySection(mappedChanges, newCacheModel);
				}
			}
		});
	}
}
