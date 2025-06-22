package com.dsl.classgen.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public class Reader {
	
    private Reader() {}

    public static void read(Path inputPath) {
    	// se for um arquivo, deve carregar ele no objeto de propriedades diretamente
    	// se for um diretorio, deve chamar o metodo que processa a lista de arquivos no diretorio
        if (Files.isRegularFile(inputPath)) {
            Reader.loadPropFile(inputPath);
            Values.setIsSingleFile(true);
            
        } else if (Files.isDirectory(inputPath)) {
            Reader.processFileList(inputPath);
        }
    }

    // carrega o arquivo de propriedades
    public static void loadPropFile(Path inputPath) {
        try {
            Utils.getExecutor().submit(() -> {
                Properties props = Values.getProps();
                try (InputStream in = Files.newInputStream(inputPath)){
                    if (!props.isEmpty()) {
                        props.clear();
                    }
                    props.load(in);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.format("%n%n***Properties file loaded from path: %s***%n", inputPath);
            }).get();
            
            Reader.processFilePath(inputPath);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
            	Thread.currentThread().interrupt();
            }
        }
    }

    // processa a string de caminho do arquivo de propriedade
    private static void processFilePath(Path inputPath) throws InterruptedException, ExecutionException {
        Utils.getExecutor().submit(() -> {
            Values.setPropertiesDataType(Utils.readJavaType(inputPath));
            Values.setRawPropertiesfileName(inputPath.getFileName());
            Values.setSoftPropertiesfileName(Utils.formatFileName(inputPath));
        }).get();
    }

    // processa a lista de arquivos contida em um diretorio e/ou subdiretorios
    private static void processFileList(Path inputPath) {
        try {
            FileVisitorImpl.ReaderFileVisitor fileVisitor = new FileVisitorImpl.ReaderFileVisitor();
            if (Values.isRecursive()) {
                Files.walkFileTree(inputPath, fileVisitor);
            } else {
                try (Stream<Path> pathStream = Files.list(inputPath);){
                    Values.setFileList(pathStream.filter(path -> Files.isRegularFile(path))
						                    	 .filter(fileVisitor.testPath::test)
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

