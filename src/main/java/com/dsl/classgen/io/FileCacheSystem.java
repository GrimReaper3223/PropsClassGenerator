package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;

public final class FileCacheSystem {
    public static void processCache() {
        try {
            boolean isCacheDirValid = Files.exists(Values.getCacheDirs()) && Files.size(Values.getCacheDirs()) > 0L;
            if (isCacheDirValid && Values.isDirStructureAlreadyGenerated()) {
                FileCacheSystem.loadCache();
                
            } else if (isCacheDirValid && !Values.isDirStructureAlreadyGenerated()) {
            	System.out.println("\nCache exists, but directory structure does not exist. Revalidating cache...");
                FileCacheSystem.eraseCache();
                FileCacheSystem.createCache();
                
            } else {
            	System.out.println("\nCache does not exist. Generating new cache structure...");
                FileCacheSystem.createCache();
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
}

