package com.dsl.test.classgen;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.dsl.classgen.utils.Utils;

public interface HarnessTestTools {

	Path inPropsPath = Path.of("src/test/resources/values/stringsx");
	String PACKAGE_CLASS = "com.dsl.test.classgen";
	Path cachePath = Paths.get(System.getProperty("user.dir")).resolve(".jsonProperties-cache");
	Path sourceDirPath = Path.of("src/test/java/com/dsl/test/classgen/generated");
	Path sourceFilePath = sourceDirPath.resolve("P.java");
	Path classPath = Path.of("target/test-classes", sourceDirPath.subpath(3, sourceDirPath.getNameCount()).toString(),
			"P.class");

	default void eraseCache() {
		if (Files.exists(cachePath)) {
			try {
				Files.walkFileTree(cachePath, new SimpleFileVisitor<Path>() {
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
					if (Utils.getSafetyFileName(file, null).toString().equals("P.java")) {
						Files.delete(file);
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (Utils.getSafetyFileName(dir, null).toString().equals("generated") && Files.size(dir) == 0) {
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