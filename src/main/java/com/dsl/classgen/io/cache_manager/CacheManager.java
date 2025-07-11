package com.dsl.classgen.io.cache_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.FileVisitorImpl;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public class CacheManager {

	private static final Logger LOGGER = LogManager.getLogger(CacheManager.class);
	
	private static Map<Path, CacheModel> cacheModelMap = new HashMap<>();  	// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache 
	private static Deque<Path> newCacheToWrite = new ArrayDeque<>();		// deve armazenar arquivos para processamento de cache. Quando novos arquivos forem fornecidos para a lista de arquivos ou individualmente, uma entrada correspondente deve ser criada aqui
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	
	private CacheManager() {}
	
	public static void addNewCacheFileToWrite(Path filePath) {
		newCacheToWrite.offer(filePath);
	}
	
	public static List<Path> getAllCacheFilesToWrite() {
    	List<Path> cacheList = new ArrayList<>();
    	
    	while(!newCacheToWrite.isEmpty()) {
    		cacheList.add(newCacheToWrite.poll());
    	}
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
			e.printStackTrace();
		}
		return cacheModelMap.remove(key);
	}
	
	public static boolean containsCacheForProcessing() {
    	return !newCacheToWrite.isEmpty();
    }
	
	// retorna true se o cache ja existe e se e identico ao arquivo atualmente lido
	// false se uma das condicionais falharem
	public static boolean hasValidCacheFile(Path propsPath) {
		Path jsonFilePath = Utils.resolveJsonFilePath(propsPath);
		boolean result = false;
		
		if(Files.exists(jsonFilePath)) {
			Reader.loadPropFile(propsPath);
			try(BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				CacheModel htm = new CacheModel(propsPath, fwCtx.getProps());
				result = htm.equals(new Gson().fromJson(br, CacheModel.class));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static void processCache() {
        try {
        	Path cacheDir = pathsCtx.getCacheDir();
            boolean isCacheDirValid = Files.exists(cacheDir) && Files.size(cacheDir) > 0L;
            
            if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated() && containsCacheForProcessing()) {
            	LOGGER.log(Level.WARN, "Updating cache...\n");
            	updateCache();
            	
            	if(cacheModelMap.size() == 0) {
            		loadCache();
            	}
            	
            } else if (isCacheDirValid && flagsCtx.getIsDirStructureAlreadyGenerated()) {
                loadCache();
                
            } else if (isCacheDirValid && !flagsCtx.getIsDirStructureAlreadyGenerated()) {
            	LOGGER.log(Level.WARN, "Cache exists, but directory structure does not exist. Revalidating cache...\n");
                eraseCache();
                createCache();
                
            } else {
            	LOGGER.log(Level.WARN, "Cache does not exist. Generating new cache structure...\n");
                createCache();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

	private static void loadCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpl.CacheReaderFileVisitor());
	}

	private static void eraseCache() throws IOException {
		Files.walkFileTree(pathsCtx.getCacheDir(), new FileVisitorImpl.CacheEraserVisitor());
	}

	private static void createCache() throws IOException {
		Files.createDirectories(pathsCtx.getCacheDir());
		Writer.writeJson();
	}

	private static void updateCache() {
		Writer.writeJson();
	}
}
