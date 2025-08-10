package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.io.FileVisitorImpls;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

/**
 * The Class Reader.
 */
public final class Reader extends SupportProvider {

	private Reader() {}

	/**
	 * Reads a properties file from a path passed as an argument.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param path the properties file path
	 */
	public static <T> void read(T path) {
		Path inputPath = Path.of(path.toString());

		if (Files.isRegularFile(inputPath) && Utils.isPropertiesFile(inputPath)) {
			flagsCtx.setIsSingleFile(true);
			pathsCtx.queueFile(inputPath);

		} else if (Files.isDirectory(inputPath)) {
			flagsCtx.setIsSingleFile(false);
			processDirectoryFileList(inputPath);
		}
	}

	/**
	 * Reads the source file that contains all the mapped property file data
	 *
	 * @param <T>        the generic type to be associated with the argument (String
	 *                   or Path)
	 * @param sourceFile the generated source file
	 * @return the string containing the data from the read file
	 */
	public static <T> String readSource(T sourceFile) {
		StringBuilder sourceBuffer = new StringBuilder();

		try (Stream<String> lines = Files.lines(Path.of(sourceFile.toString()))) {
			lines.forEach(line -> sourceBuffer.append(line + '\n'));
		} catch (IOException e) {
			Utils.logException(e);
		}

		return sourceBuffer.toString();
	}

	/**
	 * Loads a properties file into a properties object
	 *
	 * @param <T>  the generic type to be associated with the argument (String or
	 *             Path)
	 * @param path the properties file path
	 * @return the properties object loaded from the file
	 */
	public static <T> Properties loadProp(T path) {
		Path inputPath = Path.of(path.toString());
		Properties props = new Properties();

		try (InputStream in = Files.newInputStream(inputPath)) {
			props.load(in);
			LOGGER.log(LogLevels.SUCCESS.getLevel(), "Properties file loaded from path: {}", inputPath);

		} catch (IOException e) {
			Utils.logException(e);
		}

		return props;
	}

	/**
	 * Loads the compiled class file containing all mapped properties
	 *
	 * @return the compiled class file loaded
	 */
	public static Class<?> loadGeneratedBinClass() {
		Class<?> generatedClass = null;
		String fullPackageClass = pathsCtx.getFullPackageClass().replace(".java", "");
		try {
			URLClassLoader classLoader = new URLClassLoader(
					new URL[] { pathsCtx.getOutputClassFilePath().toUri().toURL() },
					ClassLoader.getPlatformClassLoader());
			generatedClass = Class.forName(fullPackageClass, true, classLoader);
		} catch (ClassNotFoundException | MalformedURLException e) {
			Utils.logException(e);
		}
		return generatedClass;
	}

	/**
	 * Read the java type defined by the developer in the provided property file
	 *
	 * @param <T>  the generic type to be associated with the argument (String or
	 *             Path)
	 * @param path the properties file path
	 * @return the string containing the java type
	 * @throws InterruptedException the interrupted exception if the thread is
	 *                              interrupted
	 * @throws ExecutionException   the execution exception if the computation threw
	 *                              an exception
	 */
	public static <T> String readJavaType(T path) throws InterruptedException, ExecutionException {
		return Utils.getExecutor()
				.submit(() -> Files.readAllLines(Path.of(path.toString()), StandardCharsets.ISO_8859_1).stream()
						.filter(input -> input.contains("$javatype:")).findFirst()
						.map(input -> input.substring(input.indexOf("@") + 1))
						.orElseThrow(GeneralContext::throwIOException))
				.get();
	}

	/**
	 * Processes the list of files contained in the passed directory path.
	 *
	 * @param inputDirPath the input dir path
	 */
	private static void processDirectoryFileList(Path inputDirPath) {
		try {
			if (flagsCtx.getIsRecursive()) {
				Files.walkFileTree(inputDirPath, new FileVisitorImpls.ReaderFileVisitor());
			} else {
				try (Stream<Path> pathStream = Files.list(inputDirPath)) {
					pathStream.filter(Utils.fileFilter::test).forEach(pathsCtx::queueFile);
				}
				pathsCtx.queueDir(inputDirPath);
			}
		} catch (IOException e) {
			Utils.logException(e);
		}
	}
}
