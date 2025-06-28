package com.dsl.classgen.io.cache_system;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.io.FileVisitorImpl;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.file_handler.Writer;

public final class FileCacheSystem {
	private static final Logger LOGGER = LogManager.getLogger(FileCacheSystem.class);
	
    public static void processCache() {
        try {
            boolean isCacheDirValid = Files.exists(Values.getCacheDir()) && Files.size(Values.getCacheDir()) > 0L;
            if (isCacheDirValid && Values.isDirStructureAlreadyGenerated() && Values.containsCacheToProcess()) {
            	LOGGER.log(Level.WARN, "Updating cache...\n");
            	updateCache();
            	
            	if(Values.getHashTableMapSize() == 0) {
            		loadCache();
            	}
            	
            } else if (isCacheDirValid && Values.isDirStructureAlreadyGenerated()) {
                loadCache();
                
            } else if (isCacheDirValid && !Values.isDirStructureAlreadyGenerated()) {
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
        Files.walkFileTree(Values.getCacheDir(), new FileVisitorImpl.CacheReaderFileVisitor());
    }

    private static void eraseCache() throws IOException {
        Files.walkFileTree(Values.getCacheDir(), new FileVisitorImpl.CacheEraserVisitor());
    }

    private static void createCache() throws IOException {
        Files.createDirectories(Values.getCacheDir());
        Writer.writeJson();
    }
    
    private static void updateCache() throws IOException {
    	Writer.writeJson();
    }
}

