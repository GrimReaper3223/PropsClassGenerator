package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

/**
 * The Class CacheManager.
 */
public final class CacheManager extends SupportProvider {

	private static ConcurrentMap<Path, CacheModel> cacheModelMap = new ConcurrentHashMap<>();
	private static BlockingQueue<Path> cacheFilesToWrite = new ArrayBlockingQueue<>(1024);

	private CacheManager() {}

	/**
	 * Queue new cache file.
	 *
	 * @param <T>      the generic type to be associated with the argument (String
	 *                 or Path)
	 * @param filePath the properties file path
	 */
	public static <T> void queueNewFileToCreateCache(T filePath) {
		Path path = Path.of(filePath.toString());
		if (!cacheFilesToWrite.offer(path)) {
			LOGGER.log(LogLevels.CACHE.getLevel(), "Queue for cache files is full. Processing files...");
			Writer.writeJson();
			LOGGER.log(LogLevels.CACHE.getLevel(), "Re-queueing files for cache writing...");
			queueNewFileToCreateCache(filePath);
		}
	}

	/**
	 * Gets the cache model map entries.
	 *
	 * @return the cache model map entries
	 */
	public static Set<Entry<Path, CacheModel>> getCacheModelMapEntries() {
		return cacheModelMap.entrySet();
	}

	/**
	 * Gets the queued cache files.
	 *
	 * @param drain if true, drains the queue and returns the files, otherwise
	 *              returns the current queue without draining
	 * @return the queued cache files
	 */
	public static List<Path> getQueuedCacheFiles(boolean drain) {
		if (!drain) {
			return cacheFilesToWrite.stream().toList();
		}
		List<Path> cacheList = new ArrayList<>();
		cacheFilesToWrite.drainTo(cacheList);
		return cacheList;
	}

	/**
	 * Checks for cache to write.
	 *
	 * @return true, if exists cache to write
	 */
	public static boolean hasCacheToWrite() {
		return !cacheFilesToWrite.isEmpty();
	}

	/**
	 * Compute cache model to map.
	 *
	 * @param <T>     the generic type to be associated with the argument (String or
	 *                Path)
	 * @param keyPath the properties path
	 * @param value   the new cache model value
	 */
	public static <T> CacheModel computeCacheModelToMap(T keyPath, CacheModel value) {
		Path jsonKey = Utils.toJsonFilePath(keyPath);
		if (cacheModelMap.computeIfPresent(jsonKey, (_, _) -> value) == null) {
			cacheModelMap.put(jsonKey, value);
		}
		return value;
	}

	/**
	 * Gets the model from cache map.
	 *
	 * @param <T>     the generic type to be associated with the argument (String or
	 *                Path)
	 * @param keyPath the properties path
	 * @return the existing model from cache map
	 */
	public static <T> CacheModel getCacheModelFromMap(T keyPath) {
		Path jsonKey = Utils.toJsonFilePath(keyPath);
		return cacheModelMap.get(jsonKey);
	}

	/**
	 * Removes the element from cache model map.
	 *
	 * @param <T>     the generic type to be associated with the argument (String or
	 *                Path)
	 * @param keyPath the properties path
	 * @return the removed cache model element
	 */
	public static <T> CacheModel removeElementFromCacheModelMap(T keyPath) {
		Path jsonKey = Utils.toJsonFilePath(keyPath);
		try {
			Files.deleteIfExists(jsonKey);
		} catch (IOException e) {
			Utils.handleException(e);
		}
		return cacheModelMap.remove(jsonKey);
	}

	/**
	 * Tests the integrity of cached files.
	 * <p>
	 * If the generated directory structure and
	 * the cache file already exist, a cache model is created from this file in
	 * memory, while another already loaded in memory is retrieved from the model
	 * list.
	 * <p>
	 * An internal check is performed to verify whether they are different. <br>
	 * If so, the analyzed file is placed on the list for cache reprocessing. <br>
	 * If not, the method simply returns.
	 * <p>
	 * If there is no existing generation of the directory structure with which
	 * the framework works, then it will be assumed that there are no caches.
	 * The file received by the method is added directly to the list for processing.
	 *
	 * @param <T>       the generic type to be associated with the argument (String
	 *                  or Path)
	 * @param propsPath the properties file path
	 */
	public static <T> void testFileIntegrity(T propsPath) {
		if(flagsCtx.hasSourceStructureGenerated(false)) {
			if (Files.exists(Utils.toJsonFilePath(propsPath))) {
				CacheModel cm = new CacheModel(OutterClassModel.getModel(propsPath));
				if(cm.equals(CacheManager.getCacheModelFromMap(propsPath))) {
					return;
				}
			}
			queueNewFileToCreateCache(propsPath);
		}
	}

	/**
	 * Process cache.
	 */
	public static void processCache() {
		try {
			Path cacheDir = pathsCtx.getCacheDir();
			boolean isValidCache = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;

			if (isValidCache && hasCacheToWrite() && flagsCtx.hasDirStructureAlreadyGenerated()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Updating cache...");
				updateCache();

				if (cacheModelMap.isEmpty()) {
					LOGGER.log(LogLevels.CACHE.getLevel(), "Loading cache...");
					loadCache();
				}

			} else if (isValidCache && cacheModelMap.isEmpty() && flagsCtx.hasDirStructureAlreadyGenerated()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Loading cache...");
				loadCache();

			} else if (isValidCache && !flagsCtx.hasDirStructureAlreadyGenerated()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Invalid cache detected. Revalidating cache...");
				eraseCache();
				createCache();

			} else if(!isValidCache) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Cache does not exist. Generating new cache...");
				createCache();
			}
		} catch (IOException e) {
			Utils.handleException(e);
		}
	}

	/**
	 * Load cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void loadCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheManagerFV(false));
	}

	/**
	 * Erase cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheManagerFV(true));
	}

	/**
	 * Creates the cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void createCache() throws IOException {
		pathsCtx.getFileSet().forEach(CacheManager::queueNewFileToCreateCache);
		Files.createDirectories(pathsCtx.getCacheDir());
		Writer.writeJson();
	}

	/**
	 * Update cache.
	 */
	private static void updateCache() {
		Writer.writeJson();
	}
}
