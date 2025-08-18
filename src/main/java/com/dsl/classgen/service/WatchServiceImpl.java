package com.dsl.classgen.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

/**
 * The Class WatchServiceImpl.
 */
public class WatchServiceImpl {

	private static final Logger LOGGER = LogManager.getLogger(WatchServiceImpl.class);

	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	private static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();

	private static final Thread watchServiceThread = new Thread(WatchServiceImpl::processEvents);
	private static final Map<WatchKey, Path> keys = new HashMap<>();
	private static final Kind<?>[] EVENT_KIND_ARR = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
	private static WatchService watcher;

	private WatchServiceImpl() {}

	static {
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Initialize this service thread.
	 */
	public static void initialize() {
		if (!watchServiceThread.isAlive()) {
			watchServiceThread.setDaemon(false);
			watchServiceThread.setName("Watch Service - Thread");
			try {
				initialRegistration();
				watchServiceThread.start();
			} catch (IOException e) {
				Utils.logException(e);
			}
		}
	}

	/**
	 * Initial registration. Must be called when initializing the framework.
	 * Registers all directories configured for monitoring.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void initialRegistration() throws IOException {
		if (flagsCtx.getIsRecursive() && !flagsCtx.getIsSingleFile()) {
			pathsCtx.getDirList().stream().forEach(path -> {
				WatchKey key = null;
				try {
					key = path.register(watcher, EVENT_KIND_ARR);
				} catch (IOException e) {
					Utils.logException(e);
				}
				keys.putAll(Map.ofEntries(WatchServiceImpl.verifyKey(key, path)));
			});
		} else {
			Path inputPath = Files.isDirectory(pathsCtx.getInputPropertiesPath()) ? pathsCtx.getInputPropertiesPath()
					: pathsCtx.getInputPropertiesPath().getParent();
			WatchKey key = inputPath.register(watcher, EVENT_KIND_ARR);
			keys.putAll(Map.ofEntries(WatchServiceImpl.verifyKey(key, inputPath)));
		}
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Done");
	}

	/**
	 * Check the monitoring key created from a directory path, notifying whether the
	 * key should be updated or registered
	 *
	 * @param key  the directory watch key
	 * @param path the directory path
	 * @return an entry whose key is the WatchKey linked to the mapped directory
	 *         path
	 */
	private static Map.Entry<WatchKey, Path> verifyKey(WatchKey key, Path path) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Checking {}...", path);
		Path mappedPath = keys.get(key);

		if (mappedPath == null) {
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Registering: {}...", path);

		} else if (!path.equals(mappedPath)) {
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Updating: {} -> {}...", mappedPath, path);
		}

		return Map.entry(key, path);
	}

	/**
	 * Analyse property dir.
	 *
	 * @param dir the directory path to be analysed
	 */
	public static void analysePropertyDir(Path dir) {
		try {
			var pair = verifyKey(dir.register(watcher, EVENT_KIND_ARR), dir);
			if (keys.computeIfPresent(pair.getKey(), (_, _) -> pair.getValue()) == null) {
				keys.put(pair.getKey(), pair.getValue());
			}
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Cast.
	 *
	 * @param event the event occurred and to be casted
	 * @return the watch event of type T
	 */
	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Process events.
	 */
	private static void processEvents() {
		LOGGER.warn("Watching...");

		do {
			try {
				WatchKey key = watcher.take();

				if (!keys.containsKey(key)) {
					LOGGER.warn("WatchKey not recognized.");
					continue;
				}

				processStream(key);

				if (!key.reset()) {
					keys.remove(key);

					if (keys.isEmpty()) {
						LOGGER.warn("There are no keys remaining for processing. Ending Watcher...");
					}
				}
			} catch (InterruptedException e) {
				Utils.logException(e);
			}
		} while (!keys.isEmpty());
	}

	/**
	 * Process stream.
	 *
	 * @param key the watch key to be processed
	 */
	private static void processStream(WatchKey key) {
		key.pollEvents().stream().filter(event -> event.kind() != OVERFLOW).map(event -> {
			WatchEvent<Path> eventPath = cast(event);
			Path occurrence = keys.get(key).resolve(eventPath.context());
			return Map.entry(occurrence, eventPath.kind());
		}).filter(entry -> Utils.isPropertiesFile(entry.getKey()) || Files.isDirectory(entry.getKey()))
				.forEach(entry -> {
					LOGGER.log(LogLevels.NOTICE.getLevel(), "{}: {}", entry.getValue().name(), entry.getKey());

					try {
						pathsCtx.queueChangedFileEntry(entry);
						if (entry.getValue().name().equals("ENTRY_CREATE") && Files.isDirectory(entry.getKey())) {
							pathsCtx.queueDir(entry.getKey());
						}
					} catch (InterruptedException e) {
						Utils.logException(e);
					}
				});
	}

	/**
	 * Checks if is watch service thread alive.
	 *
	 * @return true, if is watch service thread alive
	 */
	public static boolean isWatchServiceThreadAlive() {
		return watchServiceThread.isAlive();
	}

	/**
	 * Gets the thread name.
	 *
	 * @return the thread name
	 */
	public static String getThreadName() {
		return watchServiceThread.getName();
	}
}
