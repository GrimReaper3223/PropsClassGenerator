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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.FileVisitorImpl;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public class Reader {
	
	private static final Logger LOGGER = LogManager.getLogger(Reader.class);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	
    private Reader() {}

    public static void read(Path inputPath) {
    	// se for um arquivo, deve carregar ele no objeto de propriedades diretamente
    	// se for um diretorio, deve chamar o metodo que processa a lista de arquivos no diretorio
        if (Files.isRegularFile(inputPath)) {
        	flagsCtx.setIsSingleFile(true);
            loadPropFile(inputPath);
            
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
			e.printStackTrace();
		}
    	
    	return sourceBuffer;
    }
    
    // carrega o arquivo de propriedades
    public static void loadPropFile(Path inputPath) {
        try {
            Properties props = fwCtx.getProps();
            try (InputStream in = Files.newInputStream(inputPath)) {
                if (!props.isEmpty()) {
                    props.clear();
                }
                props.load(in);
            }
            
            LOGGER.log(Level.INFO, "\n***Properties file loaded from path: {}***\n", inputPath);
                
			pathsCtx.setPropertiesDataType(readJavaType(inputPath));
            pathsCtx.setPropertiesFileName(inputPath.getFileName());
        }
        catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
            	Thread.currentThread().interrupt();
            }
        }
    }

    // processa a lista de arquivos contida em um diretorio e/ou subdiretorios
    private static void processDirectoryFileList(Path inputPath) {
        try {
            if (flagsCtx.getIsRecursive()) {
                Files.walkFileTree(inputPath, new FileVisitorImpl.ReaderFileVisitor());
            } else {
                try (Stream<Path> pathStream = Files.list(inputPath)){
                    pathStream.filter(Files::isRegularFile)
	                    	  .filter(Utils::isPropertiesFile)
	                    	  .forEach(pathsCtx::addFileToList);
                }
                pathsCtx.addDirToList(inputPath);
            }
            WatchServiceImpl.analysePropertyDirKeyPath(inputPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // le o tipo java dentro do arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    private static String readJavaType(Path path) throws InterruptedException, ExecutionException {
    	return Utils.getExecutor().submit(() -> {
	        try (Stream<String> lines = Files.lines(path, StandardCharsets.ISO_8859_1);){
	            return lines.filter(input -> input.contains("$javatype:"))
	            				.findFirst().map(input -> input.substring(input.indexOf("@") + 1))
	            				.orElseThrow(FrameworkContext::throwIOException);
	        }
    	}).get();
    }
    
    // carrega o binario da classe P.java gerado
    public static Class<?> loadGeneratedBinClass() {
        Class<?> generatedClass = null;
        String fullPackageClass = pathsCtx.getFullPackageClass().replace(".java", "");
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] {pathsCtx.getOutputClassFilePath().toUri().toURL()}, ClassLoader.getPlatformClassLoader());
            generatedClass = Class.forName(fullPackageClass , true, classLoader);
        }
        catch (ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        return generatedClass;
    }
}

