package com.dsl.classgen.io;

import java.io.BufferedReader;
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
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

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
	public static <T> void queueNewCacheFile(T filePath) {
		Path path = Path.of(filePath.toString());
		if (!cacheFilesToWrite.offer(path)) {
			Writer.writeJson();
			queueNewCacheFile(filePath);
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
	public static <T> void computeCacheModelToMap(T keyPath, CacheModel value) {
		Path jsonKey = Utils.resolveJsonFilePath(keyPath);
		if (cacheModelMap.computeIfPresent(jsonKey, (_, _) -> value) == null) {
			cacheModelMap.put(jsonKey, value);
		}
	}

	/**
	 * Gets the model from cache map.
	 *
	 * @param <T>     the generic type to be associated with the argument (String or
	 *                Path)
	 * @param keyPath the properties path
	 * @return the existing model from cache map
	 */
	public static <T> CacheModel getModelFromCacheMap(T keyPath) {
		Path jsonKey = Utils.resolveJsonFilePath(keyPath);
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
		Path jsonKey = Utils.resolveJsonFilePath(keyPath);
		try {
			Files.delete(jsonKey);
		} catch (IOException e) {
			Utils.logException(e);
		}
		return cacheModelMap.remove(jsonKey);
	}

	/**
	 * Checks if is invalid cache file.
	 *
	 * @param <T>       the generic type to be associated with the argument (String
	 *                  or Path)
	 * @param propsPath the properties file path
	 * @return true, if is invalid cache file
	 */
	public static <T> boolean isInvalidCacheFile(T propsPath) {
		Path jsonFilePath = Utils.resolveJsonFilePath(propsPath);
		boolean isInvalidCacheFile = true;

		if (Files.exists(jsonFilePath)) {
			try (BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				CacheModel cm = CacheManager.getModelFromCacheMap(jsonFilePath);
				isInvalidCacheFile = !cm.equals(new Gson().fromJson(br, CacheModel.class));
			} catch (IOException e) {
				Utils.logException(e);
			}
		}
		return isInvalidCacheFile;
	}

	/**
	 * Process cache.
	 */
	public static void processCache() {
		try {
			Path cacheDir = pathsCtx.getCacheDir();
			boolean isCacheDirValid = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;

			if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && hasCacheToWrite()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Updating cache...");
				updateCache();

				if (cacheModelMap.isEmpty()) {
					LOGGER.log(LogLevels.CACHE.getLevel(), "Loading cache...");
					loadCache();
				}

			} else if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && cacheModelMap.isEmpty()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Loading cache...");
				loadCache();

			} else if (isCacheDirValid && !flagsCtx.getIsDirStructureAlreadyGenerated()) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Invalid cache detected. Revalidating cache...");
				eraseCache();
				createCache();

			} else if (!isCacheDirValid) {
				LOGGER.log(LogLevels.CACHE.getLevel(), "Cache does not exist. Generating new cache...");
				createCache();
			}
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Load cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void loadCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheLoaderFileVisitor());
	}

	/**
	 * Erase cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheEraserVisitor());
	}

	/**
	 * Creates the cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void createCache() throws IOException {
		pathsCtx.getFileList().forEach(CacheManager::queueNewCacheFile);
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
