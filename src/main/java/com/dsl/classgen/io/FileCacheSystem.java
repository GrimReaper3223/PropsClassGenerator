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

public final class FileCacheSystem {
	
	public static void processCache() {
		try {
			if(Files.exists(Values.getCacheDirs()) && Files.size(Values.getCacheDirs()) > 0) {
				loadCache();
			} else {
				createCache();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadCache() {
		try {
			Files.walkFileTree(Values.getCacheDirs(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Utils.getExecutor().execute(() -> {
						try (BufferedReader br = Files.newBufferedReader(file)) {
							System.out.format("%n[CACHE] Loading JSON file: %s", file);
							Values.putElementIntoHashTableMap(file.toString(), Values.getGson().fromJson(br, HashTableModel.class));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.TERMINATE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createCache() {
		System.out.println("\nCache does not exist. Generating new cache structure...");
		try {
			if(Files.notExists(Values.getCacheDirs())) {
				Files.createDirectories(Values.getCacheDirs());
			}
			Writer.writeJson();
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}
}
