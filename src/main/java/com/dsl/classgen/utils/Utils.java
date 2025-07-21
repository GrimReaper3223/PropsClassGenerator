package com.dsl.classgen.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.ExtParsers;

public final class Utils {
	
	// executor que inicia uma thread virtual por task
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    private static GeneralContext generalCtx = GeneralContext.getInstance();
    private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
    private static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();

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
    
    private static <T> Path relativizePackageClassWithTargetPath(T packagePath, String appendInnerClassFileName) throws IOException {
    	Path inferPackagePath = Path.of(packagePath.toString());
    	
    	Path relativizedPath = inferPackagePath.relativize(Path.of(System.getProperty("user.dir")));
    	Path subPath = inferPackagePath.subpath(relativizedPath.getNameCount(), inferPackagePath.getNameCount());
    	Path fullPath = pathsCtx.getOutputClassFilePath().subpath(0, 2).resolve(subPath).resolve(appendInnerClassFileName != null ? pathsCtx.getOutterClassName() + "$" + appendInnerClassFileName : "");
    	
    	if(Files.exists(fullPath)) {
    		return fullPath;
    	}
    	
    	throw new IOException(String.format("Class file %s does not exist.", fullPath));
    }
    
    public static <T> Path convertSourcePathToClassPath(T sourcePath) throws IOException {
    	Path inferSourcePath = Path.of(sourcePath.toString());
    	
    	String classFileName = ExtParsers.parseClassNameHelper(inferSourcePath.getFileName(), ".class");	//PropertyFile.class
    	Path packageClassPath = flagsCtx.getIsExistsPJavaSource() ? pathsCtx.getExistingPJavaGeneratedSourcePath().getParent() : normalizePath(pathsCtx.getPackageClass(), ".", "/");
    	
    	return relativizePackageClassWithTargetPath(packageClassPath, classFileName);	//../../P$PropertyFile.class
    }

    public static <T> Path normalizePath(T path, String toReplace, String replaceWith) {
        return Path.of(path.toString().replaceAll("[" + toReplace + "]", replaceWith));
    }
}

