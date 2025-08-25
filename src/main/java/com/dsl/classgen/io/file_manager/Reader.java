package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.core.classloader.CustomClassLoader;
import com.dsl.classgen.io.FileVisitorImpls;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public final class Reader extends SupportProvider {

    private Reader() {}

    public static <T> void read(T path) {
    	Path inputPath = Path.of(path.toString());

        if (Files.isRegularFile(inputPath) && Utils.isPropertiesFile(inputPath)) {
        	flagsCtx.setIsSingleFile(true);
        	pathsCtx.queueFile(inputPath);
        	pathsCtx.queueDir(inputPath.getParent());

        } else if (Files.isDirectory(inputPath)) {
        	flagsCtx.setIsSingleFile(false);
            processDirectoryFileList(inputPath);
        }
    }

    public static <T> String readSource(T sourceFile) {
    	StringBuilder sourceBuffer = new StringBuilder();

    	try(Stream<String> lines = Files.lines(Path.of(sourceFile.toString()))) {
    		lines.forEach(line -> sourceBuffer.append(line + '\n'));
    	}
    	catch (IOException e) {
    		Utils.handleException(e);
		}

    	return sourceBuffer.toString();
    }

    public static <T> Properties loadProp(T path) {
    	Path inputPath = Path.of(path.toString());
    	Properties props = new Properties();

        try (InputStream in = Files.newInputStream(inputPath)) {
            props.load(in);
            LOGGER.log(LogLevels.SUCCESS.getLevel(), "Properties loaded from file: {}", inputPath);

        }  catch (IOException e) {
        	Utils.handleException(e);
        }

        return props;
    }

    public static Class<?> loadGeneratedBinClass() {
        Class<?> generatedClass = null;
        try {
        	generatedClass = new CustomClassLoader().loadClass(pathsCtx.getOutputClassFilePath().toString());
        }
        catch (ClassNotFoundException e) {
        	Utils.handleException(e);
        }
        return generatedClass;
    }

    public static <T> String readJavaType(T path) throws InterruptedException, ExecutionException {
    	return Utils.getExecutor().submit(
    			() -> Files.readAllLines(Path.of(path.toString()), StandardCharsets.ISO_8859_1)
        				.stream()
	        			.filter(input -> input.contains("$javatype:"))
        				.findFirst()
        				.map(input -> input.substring(input.indexOf("@") + 1))
        				.orElseThrow(GeneralContext::throwIOException)
        ).get();
    }

    private static void processDirectoryFileList(Path inputDirPath) {
        try {
            if (flagsCtx.getRecursiveOption()) {
                Files.walkFileTree(inputDirPath, new FileVisitorImpls.ReaderFileVisitor());
            } else {
                try (Stream<Path> pathStream = Files.list(inputDirPath)){
                    pathStream.filter(Utils.fileFilter::test).forEach(pathsCtx::queueFile);
                }
                pathsCtx.queueDir(inputDirPath);
            }
        }
        catch (IOException e) {
        	Utils.handleException(e);
        }
    }
}
