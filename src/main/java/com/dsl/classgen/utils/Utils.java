package com.dsl.classgen.utils;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dsl.classgen.io.Values;

public final class Utils {
	
	// executor que inicia uma thread virtual por task
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static ExecutorService getExecutor() {
        return executor;
    }

    // calcula o tempo decorrido de uma operacao de geracao
    public static long calculateElapsedTime() {
        if (Values.getStartTimeOperation() == 0L) {
            Values.setStartTimeOperation(System.currentTimeMillis());
            return 0L;
        }
        if (Values.getEndTimeOperation() == 0L) {
            Values.setEndTimeOperation(System.currentTimeMillis());
        }
        return Values.getEndTimeOperation() - Values.getStartTimeOperation();
    }

    /*
     * Utilitarios para formatacao de caminhos e strings por outras partes do sistema
     */
    
    public static boolean isPropertiesFile(Path filePath) {
    	return filePath.getFileName().toString().endsWith(".properties");
    }
    
    public static Path resolveJsonFilePath(Path fileName) {
    	if(fileName.toString().contains(".")) {
    		fileName = formatFileName(fileName);
    	}
    	Path jsonFileName = Path.of(String.format(Values.getJsonFilenamePattern(), fileName));
        return Values.getCacheDirs().resolve(jsonFileName);
    }

    public static Path formatFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return Path.of(fileName.substring(0, fileName.lastIndexOf(".")));
    }

//    public static String extractPackageName(Path path) {
//        return path.toString().replaceAll("[\\W\\D]*/java/", "");
//    }

    public static <T> Path normalizePath(T path, String toReplace, String replaceWith) {
    	toReplace = "[" + toReplace + "]";
        return path instanceof String s ? Path.of(s.replaceAll(toReplace, replaceWith)) : 
        	Path.of(String.valueOf(path).replaceAll(toReplace, replaceWith));
    }
}

