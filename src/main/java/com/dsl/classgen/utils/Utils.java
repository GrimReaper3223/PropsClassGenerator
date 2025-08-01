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

public final class Utils {
	
	private static final Logger LOGGER = LogManager.getLogger(Utils.class);
	
	// executor que inicia uma thread virtual por task
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    private static GeneralContext generalCtx = GeneralContext.getInstance();
    private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

    public static final Predicate<Path> fileFilter = path -> Files.isRegularFile(path) && Utils.isPropertiesFile(path);
    
    private Utils() {}
    
    public static ExecutorService getExecutor() {
        return executor;
    }

    // calcula o tempo decorrido de uma operacao de geracao
    public static long calculateElapsedTime() {
        if (generalCtx.getTimeOperation() == 0L) {
        	generalCtx.setTimeOperation(System.currentTimeMillis());
            return 0L;
        }
        return System.currentTimeMillis() - generalCtx.getTimeOperation();
    }

    /*
     * Utilitarios para formatacao de caminhos e strings por outras partes do sistema
     */
    public static boolean isPropertiesFile(Path filePath) {
    	return filePath.getFileName().toString().endsWith(".properties");
    }
    
    public static <T> Path resolveJsonFilePath(T path) {
    	Path filePath = Path.of(path.toString());
    	if (filePath.getFileName().toString().contains("-cache.json")) {
    		return filePath;
    	}
    	String jsonFileNamePattern = "%s-cache.json";
    	Path jsonFileName = Path.of(String.format(jsonFileNamePattern, filePath.getFileName().toString().contains(".") ? formatFileName(filePath) : filePath));
        return pathsCtx.getCacheDir().resolve(jsonFileName);
    }

    public static <T> Path formatFileName(T filePath) {
        String fileName = Path.of(filePath.toString()).getFileName().toString();
        return Path.of(fileName.substring(0, fileName.lastIndexOf(".")));
    }
    
    public static String formatSourcePattern(PatternType type, String path) {
    	return String.format("// %s HINT ~>> %s@// %1$s HINT <<~ %2$s", type.name(), path);
    }
    
    public static <T> Path convertSourcePathToClassPath(T sourcePath) throws ClassNotFoundException {
    	String classFileName = new Parsers() {}.parseClassName(Path.of(sourcePath.toString()).getFileName());
		return Arrays.stream(Reader.loadGeneratedBinClass().getClasses())
			  .filter(cls -> cls.getName().contains(classFileName))
			  .map(cls -> Path.of(pathsCtx.getOutputClassFilePath()
					  			  .subpath(0, 2)
					  			  .resolve(normalizePath(cls.getName(), ".", "/"))
					  			  .toString()
					  			  .concat(".class")))
			  .findFirst()
			  .orElseThrow(ClassNotFoundException::new);
    }

    public static <T> Path normalizePath(T path, String toReplace, String replaceWith) {
        return Path.of(path.toString().replaceAll("[" + toReplace + "]", replaceWith));
    }
    
    public static void logException(Exception e) {
    	if(e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
    		LOGGER.error("Thread is interrupted.", e);
        	Thread.currentThread().interrupt();
        } else {
        	LOGGER.catching(e);
        }
    }
}

