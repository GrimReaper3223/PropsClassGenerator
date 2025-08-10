package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

/**
 * The Class Writer.
 */
public final class Writer extends SupportProvider {

	/** The Constant StandardOpenOptions. */
	private static final StandardOpenOption[] OPTS = {
			StandardOpenOption.CREATE,
			StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING
	};

	private Writer() {}

	/**
	 * Write first generation.
	 */
	public static void writeFirstGeneration() {
		Path outputPackagePath = pathsCtx.getOutputSourceDirPath();
		Path outputFilePath = pathsCtx.getOutputSourceFilePath();

		try {
			Files.createDirectories(outputPackagePath);
			Writer.write(outputFilePath, pathsCtx.getGeneratedClass());
			LOGGER.log(LogLevels.SUCCESS.getLevel(), "***File created in: {} [Elapsed Time: {}ms]***\n",
					outputPackagePath, Utils.calculateElapsedTime());
		} catch (IOException e) {
			Utils.logException(e);
		} finally {
			pathsCtx.setExistingPJavaGeneratedSourcePath(outputFilePath);
		}
	}

	/**
	 * Provides means to write Strings or byte arrays to a specified path.
	 *
	 * @param <T>         the generic type to be associated with the parameter
	 *                    pathToWrite (String or Path)
	 * @param <U>         the generic type to be associated with the parameter
	 *                    content (String or byte[])
	 * @param pathToWrite the path to write the content
	 * @param content     the content to be written to the specified path
	 */
	public static <T, U> void write(T pathToWrite, U content) {
		Path path = Path.of(pathToWrite.toString());

		try {
			StandardOpenOption[] options = { StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING };

			Utils.getExecutor().submit(() -> {
				try {
					switch (content) {
						case String s -> {
							LOGGER.info("Writing data...\n");
							Files.writeString(path, s, options);
						}
						case byte[] b -> {
							LOGGER.info("Writing byte data...\n");
							Files.write(path, b);
						}
						default -> throw new IllegalArgumentException("Unexpected value: " + content);
					}
				} catch (IOException | IllegalArgumentException e) {
					Utils.logException(e);
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Write json cache.
	 */
	public static void writeJson() {
		LOGGER.log(LogLevels.CACHE.getLevel(), "Writing cache...\n");
		Gson gson = new Gson();

		CacheManager.getQueuedCacheFiles(true).stream().map(InnerStaticClassModel::initInstance).forEach(model -> {
			try {
				Files.writeString(Utils.resolveJsonFilePath(model.annotationMetadata().filePath()),
						gson.toJson(new CacheModel(model)), OPTS);
			} catch (IOException e) {
				Utils.logException(e);
			}
		});
	}
}