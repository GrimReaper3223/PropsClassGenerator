package com.dsl.classgen.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

/**
 * The Class FileVisitorImpls.
 */
public final class FileVisitorImpls extends SupportProvider {

	private FileVisitorImpls() {}

	/**
	 * The Class CacheManagerFileVisitor.
	 *
	 * @author Deiv
	 * @version 0.2.4
	 * @since 0.2.4-R1
	 */
	public static class CacheManagerFV extends SimpleFileVisitor<Path> {

		private final boolean eraseCache;

		public CacheManagerFV(boolean eraseCache) {
			this.eraseCache = eraseCache;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if(!eraseCache) {
				try {
					Utils.getExecutor().submit(() -> {
						try (BufferedReader br = Files.newBufferedReader(file)) {
							LOGGER.log(LogLevels.CACHE.getLevel(), "Loading JSON file: {}", file);
							CacheManager.computeCacheModelToMap(file, new Gson().fromJson(br, CacheModel.class));
						} catch (IOException e) {
							Utils.handleException(e);
						}
					}).get();
				} catch (InterruptedException | ExecutionException e) {
					Utils.handleException(e);
				}
			} else {
				Files.delete(file);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			if(eraseCache) {
				Files.delete(dir);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			Utils.handleException(exc);
			return FileVisitResult.TERMINATE;
		}
	}

	/**
	 * The Class ReaderFileVisitor.
	 *
	 * @author Deiv
	 * @version 0.2.4
	 * @since 0.2.4-R1
	 */
	public static class FileSystemReaderFV extends SimpleFileVisitor<Path> {

		private final Path inputDirPath;

		public FileSystemReaderFV(Path inputDirPath) {
			this.inputDirPath = inputDirPath;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (flagsCtx.getRecursiveOption() || dir.equals(inputDirPath)) {
				pathsCtx.queueDir(dir);
				return FileVisitResult.CONTINUE;
			}
			return FileVisitResult.SKIP_SUBTREE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Utils.isPropertiesFile(file)) {
				pathsCtx.queueFile(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public static class SourceFinderFV extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if(dir.getFileName().toString().equals("generated")) {
				flagsCtx.setHasDirStructureAlreadyGenerated(true);
				pathsCtx.setOutputSourceDirPath(dir);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if(file.getFileName().toString().equals(pathsCtx.getOutterClassName() + ".java")) {
				flagsCtx.setHasExistsPJavaSource(true);
				pathsCtx.setExistingPJavaGeneratedSourcePath(file);
			}
			return flagsCtx.hasSourceStructureGenerated(false) ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
	}

	public static class CompilationFinderFV extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if(file.getFileName().toString().equals(pathsCtx.getOutterClassName() + ".class")) {
				flagsCtx.setIsExistsCompiledPJavaClass(true);
				pathsCtx.setOutputClassFilePath(file);
				return FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
