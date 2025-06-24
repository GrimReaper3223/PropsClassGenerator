package com.dsl.classgen.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.utils.Utils;

public class FileVisitorImpl {

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
                        System.out.format("%n[CACHE] Loading JSON file: %s", file);
                        Values.putElementIntoHashTableMap(file, Values.getGson().fromJson(br, HashTableModel.class));
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
            Values.addDirToList(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (Utils.isPropertiesFile(file)) {
                Values.addFileToList(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

