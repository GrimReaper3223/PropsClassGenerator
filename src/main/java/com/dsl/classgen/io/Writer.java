package com.dsl.classgen.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.dsl.classgen.utils.Utils;

public class Writer {

	private static Function<String, Path> resolveJsonFileName = fileName -> Path.of(String.format(Values.getJsonFilenamePattern(), fileName));
	
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
				System.out.format("%nFile already exists in: %s [Elapsed Time: %dms]", outputPackagePath, Utils.calculateElapsedTime());
				return;
			}
			writer(outputFilePath);
			System.out.format("%nFile created in: %s [Elapsed Time: %dms]", outputPackagePath, Utils.calculateElapsedTime());
		} catch (IOException | ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			if(Thread.currentThread().isInterrupted()) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private static final void writer(Path outputFilePath) throws InterruptedException, ExecutionException {
		Utils.getExecutor().submit(() -> {
			try {
				try(OutputStream out = Files.newOutputStream(outputFilePath)) {
					out.write(Values.getGeneratedClass().getBytes());
				} finally {
					Values.setExistingPJavaGeneratedSourcePath(outputFilePath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).get();
	}
	
	public static void writeJson() throws InterruptedException, ExecutionException {
		Utils.getExecutor().submit(() -> {
			if(Values.isSingleFile()) {
				Path jsonFilePath = Values.getCacheDirs().resolve(resolveJsonFileName.apply(Values.getPropertiesFileName()));
				
				HashTableModel htm = new HashTableModel(Values.getInputPropertiesPath());
				htm.initPropertyMapGraph();
				htm.hashTableMap.entrySet().forEach(entrySet -> Values.putElementIntoHashTableMap(entrySet.getKey(), htm));
				
				try(OutputStream out = Files.newOutputStream(jsonFilePath)) {
					out.write(Values.getGson().toJson(htm).getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} else {
				Values.getFileList()
					  .stream()
					  .map(path -> {
						  Reader.loadPropFile(path);	// carrega temporariamente o arquivo de propriedades
						  Path jsonFilePath = Values.getCacheDirs().resolve(resolveJsonFileName.apply(Utils.formatFileName(path)));
						  HashTableModel htm = new HashTableModel(path);
						  htm.initPropertyMapGraph();
						  Map.Entry<Path, HashTableModel> entryModel = Map.entry(Values.getCacheDirs().resolve(jsonFilePath), htm); 
						  return entryModel;
					  })
					  // pipeline dividido em 2 para melhor manutencao
					  .map(entry -> {					// processa todos os elementos do HashTableModel carregado no mapa de elementos
						  entry.getValue()
						  	   .hashTableMap
						  	   .entrySet()
						  	   .forEach(entrySet -> Values.putElementIntoHashTableMap(entrySet.getKey(), entry.getValue()));
						  return entry;
					  })
					  .forEach(entry -> {				// escreve os arquivos json no diretorio de cache
						  try(OutputStream out = Files.newOutputStream(entry.getKey())) {
							  String json = Values.getGson().toJson(entry.getValue());
							  out.write(json.getBytes());
						  } catch (IOException e) {
							  e.printStackTrace();
						  }
					  });
			}
		}).get();
	}
}
