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
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;

public final class Reader extends SupportProvider {
	
    private Reader() {}

    public static void read(Path inputPath) {
        if (Files.isRegularFile(inputPath)) {
        	flagsCtx.setIsSingleFile(true);
        	pathsCtx.queueFile(inputPath);
            
        } else if (Files.isDirectory(inputPath)) {
        	flagsCtx.setIsSingleFile(false);
            processDirectoryFileList(inputPath);
        }
    }

    public static StringBuilder readSource(Path sourceFile) {
    	StringBuilder sourceBuffer = new StringBuilder();
    	
    	try(Stream<String> lines = Files.lines(sourceFile)) {
    		lines.forEach(line -> sourceBuffer.append(line + '\n'));
    	} 
    	catch (IOException e) {
    		logException(e);
		}
    	
    	return sourceBuffer;
    }
    
    public static void loadPropFile(Path inputPath) {
        try {
            Properties props = generalCtx.getProps();
            try (InputStream in = Files.newInputStream(inputPath)) {
                if (!props.isEmpty()) {
                    props.clear();
                }
                props.load(in);
            }
            
            LOGGER.log(Levels.SUCCESS.getLevel(), "Properties file loaded from path: {}", inputPath);
                
			pathsCtx.setPropertiesDataType(readJavaType(inputPath));
            pathsCtx.setPropertiesFileName(inputPath.getFileName());
        }
        catch (InterruptedException | ExecutionException | IOException e) {
        	logException(e);
        }
    }

    public static Class<?> loadGeneratedBinClass() {
        Class<?> generatedClass = null;
        String fullPackageClass = pathsCtx.getFullPackageClass().replace(".java", "");
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] {pathsCtx.getOutputClassFilePath().toUri().toURL()}, ClassLoader.getPlatformClassLoader());
            generatedClass = Class.forName(fullPackageClass , true, classLoader);
        }
        catch (ClassNotFoundException | MalformedURLException e) {
        	logException(e);
        }
        return generatedClass;
    }
    
    private static void processDirectoryFileList(Path inputDirPath) {
        try {
            if (flagsCtx.getIsRecursive()) {
                Files.walkFileTree(inputDirPath, new FileVisitorImpls.ReaderFileVisitor());
            } else {
                try (Stream<Path> pathStream = Files.list(inputDirPath)){
                    pathStream.filter(Files::isRegularFile)
	                    	  .filter(Utils::isPropertiesFile)
	                    	  .forEach(pathsCtx::queueFile);
                }
                pathsCtx.queueDir(inputDirPath);
            }
        }
        catch (IOException e) {
        	logException(e);
        }
    }

    private static String readJavaType(Path path) throws InterruptedException, ExecutionException {
    	return Utils.getExecutor().submit(() -> {
	        try (Stream<String> lines = Files.lines(path, StandardCharsets.ISO_8859_1);){
	            return lines.filter(input -> input.contains("$javatype:"))
	            				.findFirst().map(input -> input.substring(input.indexOf("@") + 1))
	            				.orElseThrow(GeneralContext::throwIOException);
	        }
    	}).get();
    }
    
    private static void logException(Exception e) {
    	if(e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
    		LOGGER.error("Thread is interrupted.", e);
        	Thread.currentThread().interrupt();
        } else {
        	LOGGER.fatal(e);
        }
    }
}
