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
            Values.setExistingPJavaGeneratedSourcePath(outputFilePath);
        }
    }

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

    private static final void jsonWriter(Map.Entry<Path, HashTableModel> entry) {
        Writer.jsonWriter(entry.getValue(), entry.getKey());
    }

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

