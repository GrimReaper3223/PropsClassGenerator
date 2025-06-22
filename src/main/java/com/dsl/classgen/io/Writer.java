package com.dsl.classgen.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.utils.Utils;

public class Writer {
	
    private Writer() {}

    public static void write() {
        System.out.println("\n\nWriting data...");
        Path outputPackagePath = Values.getOutputPackagePath();
        Path outputFilePath = Values.getOutputFilePath();
        
        // se nao existir o pacote de saida, entao os diretorios e o arquivo gerado serao criados
        // se o pacote e saida existir, mas nao existir o arquivo de saida, entao somente ele sera gerado
        // caso contrario, o arquivo sera marcado como existente e o metodo retorna sua execucao
        try {
            if (Files.notExists(outputPackagePath)) {
                Files.createDirectories(outputPackagePath);
                Files.createFile(outputFilePath);
                
            } else if (Files.notExists(outputFilePath)) {
                Files.createFile(outputFilePath);
                
            } else {
                System.out.format("%n***File already exists in: %s [Elapsed Time: %dms]***", outputPackagePath, Utils.calculateElapsedTime());
                return;
            }
            Writer.fileWriter(outputFilePath);
            System.out.format("%nFile created in: %s [Elapsed Time: %dms]", outputPackagePath, Utils.calculateElapsedTime());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        	// apos toda a etapa de processamento acima, podemos garantir que o caminho do arquivo de saida sera atribuido a variavel de caminho do arquivo fonte existente 
            Values.setExistingPJavaGeneratedSourcePath(outputFilePath);
        }
    }

    // escreve o arquivo de classe gerada no sistema de arquivos
    private static final void fileWriter(Path outputFilePath) {
        try {
            Utils.getExecutor().submit(() -> {
                try (OutputStream out = Files.newOutputStream(outputFilePath)){
                    out.write(Values.getGeneratedClass().getBytes());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // escreve todos os jsons gerados no sistema de arquivos, no caminho definido como o diretorio base para arquivos de cache
    private static final void jsonWriter(HashTableModel htm, Path jsonFilePath) {
        try {
            Utils.getExecutor().submit(() -> {
                try (OutputStream out = Files.newOutputStream(jsonFilePath)){
                    out.write(Values.getGson().toJson(htm).getBytes());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // metodo helper do metodo jsonWritter que recebe um Map.Entry como parametro e o desembala
    private static final void jsonWriter(Map.Entry<Path, HashTableModel> entry) {
        Writer.jsonWriter(entry.getValue(), entry.getKey());
    }

    // deve preparar todos os dados necessarios para a escrita do json
    public static void writeJson() {
        if (Values.isSingleFile()) {
            Path jsonFilePath = Values.getCacheDirs().resolve(Utils.resolveJsonFileName(Values.getSoftPropertiesFileName()));
            HashTableModel htm = new HashTableModel(Values.getInputPropertiesPath());
            Values.putElementIntoHashTableMap(jsonFilePath, htm);
            Writer.jsonWriter(htm, jsonFilePath);
            
        } else {
            Values.getFileList()
            	  .stream()
            	  .map(path -> {
		              Reader.loadPropFile(path);
		              Path jsonFilePath = Values.getCacheDirs().resolve(Utils.resolveJsonFileName(Values.getSoftPropertiesFileName()));
		              HashTableModel htm = new HashTableModel(path);
		              Map.Entry<Path, HashTableModel> entryModel = Map.entry(jsonFilePath, htm);
		              Values.putElementIntoHashTableMap(jsonFilePath, htm);
		              return entryModel;
            	  }).forEach(Writer::jsonWriter);
        }
    }
}

