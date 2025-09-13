package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.core.CustomClassLoader;
import com.dsl.classgen.io.FileVisitorImpls;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public final class Reader extends SupportProvider {

    private Reader() {}

    public static <T> void read(T path) {
    	Path inputPath = Path.of(path.toString());

        if (Utils.fileFilter.test(inputPath)) {
        	pathsCtx.queueFile(inputPath);
        	pathsCtx.queueDir(inputPath.getParent());

        } else if (Files.isDirectory(inputPath)) {
            processDirectoryFileList(inputPath);
        }
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
    	if(generalCtx.getGeneratedOutterClass() == null) {
    		try {
    			generalCtx.setGeneratedOutterClass(new CustomClassLoader().loadClass(pathsCtx.getOutputClassFilePath().toString()));
        	}
            catch (ClassNotFoundException e) {
            	Utils.handleException(e);
            }
    	}
        return generalCtx.getGeneratedOutterClass();
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
            Files.walkFileTree(inputDirPath, new FileVisitorImpls.FileSystemReaderFV(inputDirPath));
        }
        catch (IOException e) {
        	Utils.handleException(e);
        }
    }
}
