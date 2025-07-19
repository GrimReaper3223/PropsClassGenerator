package com.dsl.classgen.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class FileVisitorImpls extends SupportProvider {

	private FileVisitorImpls() {}
	
	/**
     * 
     * 
     * @since 0.2.4-R1
     * @version 0.2.4
     * @author Deiv
     */
    public static class CacheEraserVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * 
     * 
     * @since 0.2.4-R1
     * @version 0.2.4
     * @author Deiv
     */
    public static class CacheLoaderFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                Utils.getExecutor().submit(() -> {
                    try (BufferedReader br = Files.newBufferedReader(file)){
                    	LOGGER.log(Levels.CACHE.getLevel(), "Loading JSON file: {}", file);
                        CacheManager.computeElementToCacheModelMap(file, new Gson().fromJson(br, CacheModel.class));
                    }
                    catch (IOException e) {
                    	logException(e);
                    }
                }).get();
            }
            catch (InterruptedException | ExecutionException e) {
            	logException(e);
            }
            
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.TERMINATE;
        }
        
        private static void logException(Exception e) {
        	if(e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
        		LOGGER.error("Thread is interrupted.", e);
            	Thread.currentThread().interrupt();
            } else {
            	LOGGER.error(e);
            }
        }
    }

    /**
     * 
     * 
     * @since 0.2.4-R1
     * @version 0.2.4
     * @author Deiv
     */
    public static class ReaderFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        	pathsCtx.queueDir(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (Utils.isPropertiesFile(file)) {
            	pathsCtx.queueFile(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

