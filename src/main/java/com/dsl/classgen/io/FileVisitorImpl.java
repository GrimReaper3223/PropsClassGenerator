package com.dsl.classgen.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public class FileVisitorImpl {

	static FrameworkContext fwCtx = FrameworkContext.get();
	static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	
	private FileVisitorImpl() {}
	
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

    public static class CacheReaderFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                Utils.getExecutor().submit(() -> {
                    try (BufferedReader br = Files.newBufferedReader(file)){
                        System.out.format("[CACHE] Loading JSON file: %s%n", file);
                        CacheManager.computeElementToCacheModelMap(file, new Gson().fromJson(br, CacheModel.class));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }).get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
                	Thread.currentThread().interrupt();
                }
            }
            
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.TERMINATE;
        }
    }

    public static class ReaderFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        	pathsCtx.addDirToList(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (Utils.isPropertiesFile(file)) {
            	pathsCtx.addFileToList(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

