package com.dsl.classgen.io;

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

import com.dsl.classgen.utils.Utils;

public class Reader {
	
	private static final Logger LOGGER = LogManager.getLogger(Values.class);
	
    private Reader() {}

    public static void read(Path inputPath) {
    	// se for um arquivo, deve carregar ele no objeto de propriedades diretamente
    	// se for um diretorio, deve chamar o metodo que processa a lista de arquivos no diretorio
        if (Files.isRegularFile(inputPath)) {
            loadPropFile(inputPath);
            Values.setIsSingleFile(true);
            
        } else if (Files.isDirectory(inputPath)) {
            processDirectoryFileList(inputPath);
        }
    }

    // carrega o arquivo de propriedades
    public static void loadPropFile(Path inputPath) {
        try {
            Properties props = Values.getProps();
            try (InputStream in = Files.newInputStream(inputPath)){
                if (!props.isEmpty()) {
                    props.clear();
                }
                props.load(in);
            }
            
            System.out.println('\n');
            LOGGER.log(Level.INFO, "***Properties file loaded from path: {}***\n", inputPath);
                
            Values.setPropertiesDataType(readJavaType(inputPath));
            Values.setRawPropertiesfileName(inputPath.getFileName());
            Values.setSoftPropertiesfileName(Utils.formatFileName(inputPath));
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
            FileVisitorImpl.ReaderFileVisitor fileVisitor = new FileVisitorImpl.ReaderFileVisitor();
            if (Values.isRecursive()) {
                Files.walkFileTree(inputPath, fileVisitor);
            } else {
                try (Stream<Path> pathStream = Files.list(inputPath)){
                    Values.setFileList(pathStream.filter(Files::isRegularFile)
						                    	 .filter(Utils::isPropertiesFile)
						                    	 .toList());
                }
            }
            Values.addDirToList(inputPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Values.setIsSingleFile(false);
        }
    }

    // le o tipo java dentro do arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    private static String readJavaType(Path path) throws IOException, InterruptedException, ExecutionException {
    	return Utils.getExecutor().submit(() -> {
	        try (Stream<String> lines = Files.lines(path, StandardCharsets.ISO_8859_1);){
	            return lines.filter(input -> input.contains("$javatype:"))
	            				.findFirst().map(input -> input.substring(input.indexOf("@") + 1))
	            				.orElseThrow(() -> new IOException(Values.getExceptionTxt()));
	        }
    	}).get();
    }
    
    // carrega o binario da classe P.java gerado
    public static Class<?> loadGeneratedBinClass() {
        Class<?> generatedClass = null;
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] {Values.getCompilationPath().toUri().toURL()}, ClassLoader.getPlatformClassLoader());
            generatedClass = Class.forName(Values.getPackageClassWithOutterClassName(), true, classLoader);
        }
        catch (ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        return generatedClass;
    }
}

