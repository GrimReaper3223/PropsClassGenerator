package com.dsl.classgen.utils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.ExtParsers;
import com.dsl.classgen.io.file_manager.Reader;

public final class Utils {
	
	// executor que inicia uma thread virtual por task
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    private static GeneralContext generalCtx = GeneralContext.getInstance();
    private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

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
    
    public static Path resolveJsonFilePath(Path path) {
    	String jsonFileNamePattern = "%s-cache.json";
    	Path jsonFileName = Path.of(String.format(jsonFileNamePattern, path.getFileName().toString().contains(".") ? formatFileName(path) : path));
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
    	String classFileName = ExtParsers.parseClassNameHelper(Path.of(sourcePath.toString()).getFileName(), null);
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
}

