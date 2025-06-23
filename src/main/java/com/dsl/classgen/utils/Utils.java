package com.dsl.classgen.utils;

import com.dsl.classgen.io.Values;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Utils {
	
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

    // le o tipo java dentro do arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    public static String readJavaType(Path path) {
        String javaType = null;
        try (Stream<String> lines = Files.lines(path, StandardCharsets.ISO_8859_1);){
            javaType = lines.filter(input -> input.contains("$javatype:"))
            				.findFirst().map(input -> input.substring(input.indexOf("@") + 1))
            				.orElseThrow(() -> new IOException(Values.getExceptionTxt()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return javaType;
    }

    /*
     * Utilitarios para formatacao de caminhos e strings por outras partes do sistema
     */
    
    public static boolean isPropertiesFile(Path filePath) {
    	return filePath.getFileName().toString().endsWith(".properties");
    }
    
    public static Path resolveJsonFileName(String fileName) {
        return Path.of(String.format(Values.getJsonFilenamePattern(), fileName));
    }

    public static Path resolveJsonFullPath(String fileName) {
        return Values.getCacheDirs().resolve(Utils.resolveJsonFileName(fileName));
    }

    public static String formatFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String extractPackageName(String stringPath) {
        return stringPath.toString().replaceFirst("[\\w\\d/]*/src/main/java/", "");
    }

    public static <T> Path normalizePath(T path) {
        return path instanceof String s ? Path.of(s.replaceAll("[.]", "/")) : 
        	Path.of(String.valueOf(path).replaceAll("[.]", "/"));
    }
}

