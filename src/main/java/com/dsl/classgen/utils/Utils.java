package com.dsl.classgen.utils;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;

public final class Utils {
	
	// executor que inicia uma thread virtual por task
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static FrameworkContext fwCtx = FrameworkContext.get();
    private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
    

    private Utils() {}
    
    public static ExecutorService getExecutor() {
        return executor;
    }

    // calcula o tempo decorrido de uma operacao de geracao
    public static long calculateElapsedTime() {
        if (fwCtx.getTimeOperation() == 0L) {
        	fwCtx.setTimeOperation(System.currentTimeMillis());
            return 0L;
        }
        return System.currentTimeMillis() - fwCtx.getTimeOperation();
    }

    /*
     * Utilitarios para formatacao de caminhos e strings por outras partes do sistema
     */
    public static boolean isPropertiesFile(Path filePath) {
    	return filePath.getFileName().toString().endsWith(".properties");
    }
    
    public static Path resolveJsonFilePath(Path path) {
    	String jsonFileNamePattern = "%s-cache.json";
    	Path jsonFileName = Path.of(String.format(jsonFileNamePattern, path.getFileName().toString().contains(".") ? formatFileName(path) : path));
        return pathsCtx.getCacheDir().resolve(jsonFileName);
    }

    public static Path formatFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return Path.of(fileName.substring(0, fileName.lastIndexOf(".")));
    }

    public static <T> Path normalizePath(T path, String toReplace, String replaceWith) {
    	toReplace = "[" + toReplace + "]";
        return path instanceof String s ? Path.of(s.replaceAll(toReplace, replaceWith)) : 
        	Path.of(String.valueOf(path).replaceAll(toReplace, replaceWith));
    }
}

