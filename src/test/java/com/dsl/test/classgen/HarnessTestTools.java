package com.dsl.test.classgen;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public interface HarnessTestTools {

	final Path inPropsPath = Path.of("src/test/resources/values/stringsx");
	final String PACKAGE_CLASS = "com.dsl.test.classgen";
	final Path cachePath = Paths.get(System.getProperty("user.dir")).resolve(".jsonProperties-cache");
	final Path sourceDirPath = Path.of("src/test/java/com/dsl/test/classgen/generated");
	final Path sourceFilePath = sourceDirPath.resolve("P.java");
	final Path classPath = Path.of("target/test-classes", sourceDirPath.subpath(3, sourceDirPath.getNameCount()).toString(), "P.class");
	
	default void eraseCache() {
		if(Files.exists(cachePath)) {
			try {
				Files.walkFileTree(cachePath, new SimpleFileVisitor<Path> () {
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
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	default void eraseGeneratedData() {
		try {
			Files.walkFileTree(sourceDirPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(file.getFileName().toString().equals("P.java")) {
						Files.delete(file);
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if(dir.getFileName().toString().equals("generated") && Files.size(dir) == 0) {
						Files.delete(dir);
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}