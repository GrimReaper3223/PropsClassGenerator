package com.dsl.classgen.io.cache_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dsl.classgen.io.FileVisitorImpls;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class CacheManager extends SupportProvider {

	private static Map<Path, CacheModel> cacheModelMap = new HashMap<>();  					// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache 
	private static BlockingQueue<Path> cacheFilesToWrite = new ArrayBlockingQueue<>(1024);	// deve armazenar arquivos para processamento de cache. Quando novos arquivos forem fornecidos para a lista de arquivos ou individualmente, uma entrada correspondente deve ser criada aqui
	
	private CacheManager() {}
	
	public static void queueNewCacheFile(Path filePath) {
		if(!cacheFilesToWrite.offer(filePath)) {
			Writer.writeJson();
			queueNewCacheFile(filePath);
		}
	}
	
	public static List<Path> getQueuedCacheFiles() {
    	List<Path> cacheList = new ArrayList<>();
    	cacheFilesToWrite.drainTo(cacheList);
    	return cacheList;
    }
	
	public static void computeElementToCacheModelMap(Path key, CacheModel value) {
        if(cacheModelMap.computeIfPresent(key, (_, _) -> value) == null) {
        	cacheModelMap.put(key, value);
        }
    }
	
	public static CacheModel getElementFromCacheModelMap(Path key) {
        return cacheModelMap.get(key);
    }
	
	public static CacheModel removeElementFromCacheModelMap(Path key) {
		try {
			Files.deleteIfExists(key);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return cacheModelMap.remove(key);
	}
	
	// retorna true se o cache ja existe e se e identico ao arquivo atualmente lido
	// false se uma das condicionais falharem
	public static boolean hasValidCacheFile(Path propsPath) {
		Path jsonFilePath = Utils.resolveJsonFilePath(propsPath);
		boolean result = false;
		
		if(Files.exists(jsonFilePath)) {
			Reader.loadPropFile(propsPath);
			try(BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				CacheModel cm = new CacheModel(propsPath, generalCtx.getProps());
				result = cm.equals(new Gson().fromJson(br, CacheModel.class));
			} catch (IOException e) {
				LOGGER.fatal(e);
			}
		}
		return result;
	}
	
	public static void processCache() {
        try {
        	Path cacheDir = pathsCtx.getCacheDir();
            boolean isCacheDirValid = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;
            
            if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && !cacheFilesToWrite.isEmpty()) {
            	LOGGER.log(Levels.CACHE.getLevel(), "Updating cache...");
            	updateCache();
            	
            	if(cacheModelMap.size() == 0) {
            		loadCache();
            	}
            	
            } else if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated()) {
                loadCache();
                
            } else if (isCacheDirValid && !flagsCtx.getIsDirStructureAlreadyGenerated()) {
            	LOGGER.log(Levels.CACHE.getLevel(), "Cache exists, but directory structure does not exist. Revalidating cache...");
                eraseCache();
                createCache();
                
            } else if(!isCacheDirValid) {
            	LOGGER.log(Levels.CACHE.getLevel(), "Cache does not exist. Generating new cache structure...");
                createCache();
            }
        }
        catch (IOException e) {
            LOGGER.fatal(e);
        }
    }

	private static void loadCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheLoaderFileVisitor());
	}

	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpls.CacheEraserVisitor());
	}

	private static void createCache() throws IOException {
		Files.createDirectories(pathsCtx.getCacheDir());
		Writer.writeJson();
	}

	private static void updateCache() {
		Writer.writeJson();
	}
}
