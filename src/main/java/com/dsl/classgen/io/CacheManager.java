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

public final class CacheManager extends SupportProvider {

	private static ConcurrentMap<Path, CacheModel> cacheModelMap = new ConcurrentHashMap<>();  	// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache 
	private static BlockingQueue<Path> cacheFilesToWrite = new ArrayBlockingQueue<>(1024);		// deve armazenar arquivos para processamento de cache. Quando novos arquivos forem fornecidos para a lista de arquivos ou individualmente, uma entrada correspondente deve ser criada aqui
	
	private CacheManager() {}
	
	public static <T> void queueNewCacheFile(T filePath) {
		Path path = Path.of(filePath.toString());
		if(!cacheFilesToWrite.offer(path)) {
			Writer.writeJson();
			queueNewCacheFile(filePath);
		}
	}
	
	public static Set<Entry<Path, CacheModel>> getCacheModelMapEntries() {
		return cacheModelMap.entrySet();
	}
	
	public static List<Path> getQueuedCacheFiles(boolean drain) {
		if(!drain) {
			return cacheFilesToWrite.stream().toList();
		}
    	List<Path> cacheList = new ArrayList<>();
    	cacheFilesToWrite.drainTo(cacheList);
    	return cacheList;
    }
	
	public static boolean hasCacheToWrite() {
		return !cacheFilesToWrite.isEmpty();
	}
	
	public static <T> void computeCacheModelToMap(T key, CacheModel value) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
        if(cacheModelMap.computeIfPresent(jsonKey, (_, _) -> value) == null) {
        	cacheModelMap.put(jsonKey, value);
        }
    }
	
	public static <T> CacheModel getModelFromCacheMap(T key) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
        return cacheModelMap.get(jsonKey);
    }
	
	public static <T> CacheModel removeElementFromCacheModelMap(T key) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
		try {
			Files.delete(jsonKey);
		} catch (IOException e) {
			Utils.logException(e);
		}
		return cacheModelMap.remove(jsonKey);
	}
	
	/*
	 * este metodo analisa se o arquivo de cache e invalido e, por este motivo, retorna false 
	 * se o arquivo de cache for valido, ou seja, se o conteudo do cache estiver de acordo com o modelo de cache.
	 * true se o arquivo de cache for invalido, ou seja, se o conteudo do cache estiver diferente do modelo de cache
	 */
	public static <T> boolean isInvalidCacheFile(T propsPath) {
		Path jsonFilePath = Utils.resolveJsonFilePath(propsPath);
		boolean isInvalidCacheFile = true;
		
		if(Files.exists(jsonFilePath)) {
			try(BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				CacheModel cm = CacheManager.getModelFromCacheMap(jsonFilePath);
				isInvalidCacheFile = !cm.equals(new Gson().fromJson(br, CacheModel.class));
			} catch (IOException e) {
				Utils.logException(e);
			}
		}
		return isInvalidCacheFile;
	}
	
	public static void processCache() {
        try {
        	Path cacheDir = pathsCtx.getCacheDir();
            boolean isCacheDirValid = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;
            
            if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && hasCacheToWrite()) {
            	LOGGER.log(LogLevels.CACHE.getLevel(), "Updating cache...");
            	updateCache();
            	
            	if(cacheModelMap.isEmpty()) {
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
                
            } else if(!isCacheDirValid) {
            	LOGGER.log(LogLevels.CACHE.getLevel(), "Cache does not exist. Generating new cache...");
                createCache();
            }
        }
        catch (IOException e) {
        	Utils.logException(e);
        }
    }
	
	private static void loadCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheLoaderFileVisitor());
	}

	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheEraserVisitor());
	}

	private static void createCache() throws IOException {
		pathsCtx.getFileList().forEach(CacheManager::queueNewCacheFile);
		Files.createDirectories(pathsCtx.getCacheDir());
		Writer.writeJson();
	}

	private static void updateCache() {
		Writer.writeJson();
	}
}
