package com.dsl.classgen.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.models.Parsers;

/**
 * The Class Utils.
 */
public final class Utils {

	private static final Logger LOGGER = LogManager.getLogger(Utils.class);

	/** executor that starts a virtual thread per task */
	private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	public static final Predicate<Path> fileFilter = path -> Files.isRegularFile(path) && Utils.isPropertiesFile(path);

	private Utils() {}

	/**
	 * @return the executor
	 */
	public static ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * Calculate elapsed time.
	 *
	 * @return the long that informs the elapsed time of the generation operation
	 */
	public static long calculateElapsedTime() {
		if (generalCtx.getTimeOperation() == 0L) {
			generalCtx.setTimeOperation(System.currentTimeMillis());
			return 0L;
		}
		return System.currentTimeMillis() - generalCtx.getTimeOperation();
	}

	/**
	 * Checks if is properties file.
	 *
	 * @param filePath the properties file path
	 * @return true, if is properties file
	 */
	public static boolean isPropertiesFile(Path filePath) {
		return filePath.getFileName().toString().endsWith(".properties");
	}

	/**
	 * Resolve json file path.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param path the path of the properties file
	 * @return the path reference to the cache file
	 */
	public static <T> Path resolveJsonFilePath(T path) {
		Path filePath = Path.of(path.toString());
		String fileName = filePath.getFileName().toString();
		String jsonFileNamePattern = "%s-cache.json";

		if (fileName.contains("-cache.json")) {
			return filePath;
		}
		Path jsonFileName = Path.of(String.format(jsonFileNamePattern,
				fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : filePath));
		return pathsCtx.getCacheDir().resolve(jsonFileName);
	}

	/**
	 * Convert source path to class path.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param sourcePath the source path to be converted to class path
	 * @return the path of the class file corresponding to the source path
	 * @throws ClassNotFoundException if the class file cannot be found
	 */
	public static <T> Path convertSourcePathToClassPath(T sourcePath) throws ClassNotFoundException {
		String classFileName = new Parsers() {}.parseClassName(Path.of(sourcePath.toString()).getFileName());
		return Arrays.stream(Reader.loadGeneratedBinClass().getClasses())
				.filter(cls -> cls.getName().contains(classFileName))
				.map(cls -> Path.of(pathsCtx.getOutputClassFilePath().subpath(0, 2)
						.resolve(normalizePath(cls.getName(), ".", "/")).toString().concat(".class")))
				.findFirst().orElseThrow(ClassNotFoundException::new);
	}

	/**
	 * Normalize path.
	 *
	 * @param <T>         the generic type to be associated with the argument (String or Path)
	 * @param path        the path
	 * @param toReplace   string that must be found in the path to be replaced
	 * @param replaceWith string that should replace the previously mentioned string
	 * @return the normalized path
	 */
	public static <T> Path normalizePath(T path, String toReplace, String replaceWith) {
		return Path.of(path.toString().replaceAll("[" + toReplace + "]", replaceWith));
	}

	/**
	 * Log the exception.
	 *
	 * @param e the exception to log
	 */
	public static void logException(Exception e) {
		if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
			LOGGER.error("Thread is interrupted.", e);
			Thread.currentThread().interrupt();
		} else {
			LOGGER.catching(e);
		}
	}
}
