package com.dsl.classgen.io.cache_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dsl.classgen.io.FileVisitorImpls;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class CacheManager extends SupportProvider {

	private static Map<Path, CacheModel> cacheModelMap = new HashMap<>();  					// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache 
	private static BlockingQueue<Path> cacheFilesToWrite = new ArrayBlockingQueue<>(1024);	// deve armazenar arquivos para processamento de cache. Quando novos arquivos forem fornecidos para a lista de arquivos ou individualmente, uma entrada correspondente deve ser criada aqui
	private static String operationInitMode;
	
	private CacheManager() {}
	
	public static void queueNewCacheFile(Path filePath) {
		if(!cacheFilesToWrite.offer(filePath)) {
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
	
	public static void computeElementToCacheModelMap(Path key, CacheModel value) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
        if(cacheModelMap.computeIfPresent(jsonKey, (_, _) -> value) == null) {
        	cacheModelMap.put(jsonKey, value);
        }
    }
	
	public static CacheModel getElementFromCacheModelMap(Path key) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
        return cacheModelMap.get(jsonKey);
    }
	
	public static CacheModel removeElementFromCacheModelMap(Path key) {
		Path jsonKey = Utils.resolveJsonFilePath(key);
		try {
			Files.delete(jsonKey);
		} catch (IOException e) {
			LOGGER.catching(e);
		}
		return cacheModelMap.remove(jsonKey);
	}
	
	// retorna true se o cache ja existe e se e identico ao arquivo atualmente lido
	// false se uma das condicionais falharem
	public static boolean isInvalidCacheFile(Path propsPath) {
		Path jsonFilePath = Utils.resolveJsonFilePath(propsPath);
		boolean isInvalidCacheFile = true;
		
		if(Files.exists(jsonFilePath)) {
			try(BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				CacheModel cm = new CacheModel(propsPath, generalCtx.getProps());
				isInvalidCacheFile = !cm.equals(new Gson().fromJson(br, CacheModel.class));
			} catch (IOException e) {
				LOGGER.catching(e);
			}
		}
		return isInvalidCacheFile;
	}
	
	public static void processCache() {
        try {
        	Path cacheDir = pathsCtx.getCacheDir();
            boolean isCacheDirValid = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;
            
            operationInitMode = "AUTOMATIC";
            
            if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && hasCacheToWrite()) {
            	updateCache(operationInitMode);
            	
            	if(cacheModelMap.isEmpty()) {
            		loadCache(operationInitMode);
            	}
            	
            } else if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && cacheModelMap.isEmpty()) {
                loadCache(operationInitMode);
                
            } else if (isCacheDirValid && !flagsCtx.getIsDirStructureAlreadyGenerated()) {
                eraseCache();
                createCache(null);
                
            } else if(!isCacheDirValid) {
                createCache(operationInitMode);
            }
        }
        catch (IOException e) {
        	LOGGER.catching(e);
        }
    }
	
	public static void processCache(CacheProcessorOption option) {
		try {
			operationInitMode = "MANUAL";
			
			switch(option) {
				case UPDATE -> updateCache(operationInitMode); 
				case LOAD -> loadCache(operationInitMode);
				case REVALIDATE -> {
					eraseCache();
					createCache(null);
				}
				case CREATE -> createCache(operationInitMode);
			}
		} catch (IOException e) {
			LOGGER.catching(e);
		}
	}

	private static void loadCache(String appendMessage) throws IOException {
		LOGGER.log(Levels.CACHE.getLevel(), "Loading cache... ({})", appendMessage);
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheLoaderFileVisitor());
	}

	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheEraserVisitor());
	}

	private static void createCache(String appendMessage) throws IOException {
		if(appendMessage != null) {
			LOGGER.log(Levels.CACHE.getLevel(), "Cache does not exist. Generating new cache... (AUTOMATIC)");
		}
		Files.createDirectories(pathsCtx.getCacheDir());
		Writer.writeJson();
	}

	private static void updateCache(String appendMessage) {
		LOGGER.log(Levels.CACHE.getLevel(), "Updating cache... ({})", appendMessage);
		Writer.writeJson();
	}
}
