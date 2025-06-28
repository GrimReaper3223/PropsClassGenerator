package com.dsl.classgen.io.file_handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.cache_system.HashTableModel;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public class Writer {
	
	private static final Logger LOGGER = LogManager.getLogger(Writer.class);
    private Writer() {}

    public static void write() {
    	LOGGER.log(Level.INFO, "Writing data...\n");
        Path outputPackagePath = Values.getOutputSourceDirPath();
        Path outputFilePath = Values.getOutputSourceFilePath();
        
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
            	LOGGER.log(Level.WARN, "***File already exists in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
                return;
            }
            Writer.fileWriter(outputFilePath);
            LOGGER.log(Level.WARN, "***File created in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
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
                    out.write(new Gson().toJson(htm).getBytes());
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
            Path jsonFilePath = Utils.resolveJsonFilePath(Values.getSoftPropertiesFileName());
            HashTableModel htm = new HashTableModel(Values.getInputPropertiesPath(), Values.getProps());
            Values.computeElementIntoHashTableMap(jsonFilePath, htm);
            Writer.jsonWriter(htm, jsonFilePath);
            
        } else {
            Values.getAllCacheFilesToWriteFromDeque()
            	  .stream()
            	  .map(path -> {
		              Reader.loadPropFile(path);
		              Path jsonFilePath = Utils.resolveJsonFilePath(Values.getSoftPropertiesFileName());
		              HashTableModel htm = new HashTableModel(path, Values.getProps());
		              Map.Entry<Path, HashTableModel> entryModel = Map.entry(jsonFilePath, htm);
		              Values.computeElementIntoHashTableMap(jsonFilePath, htm);
		              return entryModel;
            	  }).forEach(Writer::jsonWriter);
        }
    }
}

