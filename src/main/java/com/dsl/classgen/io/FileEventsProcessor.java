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
import java.util.function.Function;
import java.util.stream.Stream;

import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.synchronizer.ModelMapper;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncOptions;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

/**
 * The Class FileEventsProcessor.
 */
public final class FileEventsProcessor extends SupportProvider {

	private static final Thread eventProcessorThread = new Thread(FileEventsProcessor::processChanges);
	private static SyncSource staticSyncSource = new SyncSource();
	private static SyncBin staticSyncBin = new SyncBin();

	/**
	 * The stream filter creator. This function creates a stream of paths from the
	 * given directory, filtering them based on the fileFilter predicate.
	 */
	private static Function<Path, Stream<Path>> streamFilterCreator = path -> {
		Stream<Path> pathStream = null;
		try {
			pathStream = Files.walk(path).filter(Utils.fileFilter::test);
		} catch (IOException e) {
			Utils.logException(e);
		}

		return pathStream;
	};

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
			try {
				var entry = pathsCtx.getQueuedChangedFilesEntries();
				var kind = entry.getValue();

				caller(kind, entry.getKey());
			} catch (InterruptedException _) {
				if (eventProcessorThread.isInterrupted()) {
					eventProcessorThread.interrupt();
					LOGGER.warn("{} is interrupted. Restarting thread...", eventProcessorThread.getName());
					initialize();
				}
			}
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
	 * @param kind the event kind for the given file
	 * @param path the file path that was changed
	 */
	public static <T> void caller(WatchEvent.Kind<Path> kind, T path) {
		Path filePath = Path.of(path.toString());

		switch (kind) {
			case Kind<Path> _ when kind.equals(ENTRY_CREATE) -> createSection(filePath);
			case Kind<Path> _ when kind.equals(ENTRY_DELETE) -> deleteSection(filePath);
			case Kind<Path> _ when kind.equals(ENTRY_MODIFY) -> modifySection(filePath);
			default -> throw new IllegalArgumentException("*** BUG *** - Unexpected value: " + path);
		}
	}

	/**
	 * Creates the section.
	 *
	 * @param path the new file path
	 */
	private static void createSection(Path path) {
		if (Files.isDirectory(path)) {
			try (Stream<Path> files = streamFilterCreator.apply(path)) {
				var fileList = files.toList();
				staticSyncSource.insertClassSection(fileList);
				staticSyncBin.insertClassSection(fileList);
			}
		} else {
			staticSyncSource.insertClassSection(path);
			staticSyncBin.insertClassSection(path);
		}
	}

	/**
	 * Delete section.
	 *
	 * @param path the file path that should be deleted
	 */
	private static void deleteSection(Path path) {
		List<CacheModel> list = new ArrayList<>();

		if (Files.isDirectory(path)) {
			LOGGER.warn("Existing directory deleted. Deleting cache and reprocessing source file entries...");

			try (Stream<Path> files = streamFilterCreator.apply(path)) {
				list.addAll(files.map(CacheManager::removeElementFromCacheModelMap).toList());
			}
		} else if (Utils.isPropertiesFile(path)) {
			LOGGER.warn("Existing file deleted. Deleting cache and reprocessing source file entries...");
			list.add(CacheManager.removeElementFromCacheModelMap(path));
		}

		staticSyncSource.eraseClassSection(list);
		staticSyncBin.eraseClassSection(list);
	}

	/**
	 * Modify section.
	 *
	 * @param path the file path that was modified
	 */
	private static void modifySection(Path path) {
		CacheModel currentCacheModel = CacheManager.getModelFromCacheMap(path);
		if (currentCacheModel == null) {
			LOGGER.error("Model (File: {}) not found in cache.", path.getFileName());
			return;
		}

		final class ExtendedCacheModel extends CacheModel {

			private static final long serialVersionUID = 1L;

			public ExtendedCacheModel(InnerStaticClassModel model) {
				super(model);
			}

			public boolean checkHash() {
				return this.fileHash == currentCacheModel.fileHash;
			}

			public boolean checkPropertyMap() {
				return this.entries.equals(currentCacheModel.entries);
			}
		}

		Path filePath = Path.of(currentCacheModel.filePath);
		Reader.read(filePath);
		ExtendedCacheModel newCacheModel = new ExtendedCacheModel(InnerStaticClassModel.initInstance(filePath));

		boolean isHashEquals = newCacheModel.checkHash();
		boolean isPropertyMapEntriesEquals = newCacheModel.checkPropertyMap();

		if (!isHashEquals) {
			if (isPropertyMapEntriesEquals) {
				deleteSection(filePath);
				createSection(filePath);

			} else {
				Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges = new ModelMapper<>()
						.mapper(currentCacheModel.entries, newCacheModel.entries);
				staticSyncSource.modifySection(mappedChanges, currentCacheModel);
				staticSyncBin.modifySection(mappedChanges, newCacheModel);
			}
		}
	}
}
