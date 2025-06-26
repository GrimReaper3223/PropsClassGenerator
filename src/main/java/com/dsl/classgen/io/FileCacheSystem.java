package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FileCacheSystem {
	private static final Logger LOGGER = LogManager.getLogger(FileCacheSystem.class);
	
    public static void processCache() {
        try {
            boolean isCacheDirValid = Files.exists(Values.getCacheDirs()) && Files.size(Values.getCacheDirs()) > 0L;
            if (isCacheDirValid && Values.isDirStructureAlreadyGenerated() && Values.containsCacheForProcess()) {
            	LOGGER.log(Level.WARN, "Updating cache...\n");
            	updateCache();
            	
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
        Files.walkFileTree(Values.getCacheDirs(), new FileVisitorImpl.CacheReaderFileVisitor());
    }

    private static void eraseCache() throws IOException {
        Files.walkFileTree(Values.getCacheDirs(), new FileVisitorImpl.CacheEraserVisitor());
    }

    private static void createCache() throws IOException {
        Files.createDirectories(Values.getCacheDirs());
        Writer.writeJson();
    }
    
    private static void updateCache() throws IOException {
    	Writer.writeJson();
    }
}

