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
	 * The Class CacheEraserVisitor.
	 *
	 * @author Deiv
	 * @version 0.2.4
	 * @since 0.2.4-R1
	 */
	public static class CacheEraserVisitor extends SimpleFileVisitor<Path> {

		/**
		 * Post visit directory.
		 *
		 * @param dir the dir
		 * @param exc the exc
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Visit file.
		 *
		 * @param file  the file
		 * @param attrs the attrs
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}
	}

	/**
	 * The Class CacheLoaderFileVisitor.
	 *
	 * @author Deiv
	 * @version 0.2.4
	 * @since 0.2.4-R1
	 */
	public static class CacheLoaderFileVisitor extends SimpleFileVisitor<Path> {

		/**
		 * Visit file.
		 *
		 * @param file  the file
		 * @param attrs the attrs
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			try {
				Utils.getExecutor().submit(() -> {
					try (BufferedReader br = Files.newBufferedReader(file)) {
						LOGGER.log(LogLevels.CACHE.getLevel(), "Loading JSON file: {}", file);
						CacheManager.computeCacheModelToMap(file, new Gson().fromJson(br, CacheModel.class));
					} catch (IOException e) {
						Utils.logException(e);
					}
				}).get();
			} catch (InterruptedException | ExecutionException e) {
				Utils.logException(e);
			}

			return FileVisitResult.CONTINUE;
		}

		/**
		 * Visit file failed.
		 *
		 * @param file the file
		 * @param exc  the exc
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			Utils.logException(exc);
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
	public static class ReaderFileVisitor extends SimpleFileVisitor<Path> {

		/**
		 * Post visit directory.
		 *
		 * @param dir the dir
		 * @param exc the exc
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			pathsCtx.queueDir(dir);
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Visit file.
		 *
		 * @param file  the file
		 * @param attrs the attrs
		 * @return the file visit result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Utils.isPropertiesFile(file)) {
				pathsCtx.queueFile(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
