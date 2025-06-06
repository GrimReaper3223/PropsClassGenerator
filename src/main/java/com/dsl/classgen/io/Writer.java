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
	
	public static void write() throws InterruptedException, ExecutionException {
		System.out.println("\n\nWriting data...");
		Path outputPath = Values.getOutputPath();
		
		if(Files.notExists(outputPath)) {
			Utils.getExecutor().submit(() -> {
				try {
					Path filePath = Files.createFile(Files.createDirectories(outputPath).resolve(Path.of(Values.getOutterClassName().concat(".java"))));
					try(OutputStream out = Files.newOutputStream(filePath)) {
						out.write(Values.getGeneratedClass().getBytes());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).get();
		}
		System.out.format("%nFile created in: %s [Elapsed Time: %dms]", outputPath, Utils.calculateElapsedTime());
	}
	
	public static void writeJson() throws InterruptedException, ExecutionException {
		Utils.getExecutor().submit(() -> {
			if(Values.isSingleFile()) {
				Path jsonFilePath = Values.getCacheDirs().resolve(resolveJsonFileName.apply(Values.getPropertiesFileName()));
				
				try(OutputStream out = Files.newOutputStream(jsonFilePath)) {
					String json = Values.getGson().toJson(new PropertyCacheModel(Values.getInputPath()));
					out.write(json.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} else {
				Values.getFileList()
					  .stream()
					  .map(path -> {
						  Path jsonFilePath = Values.getCacheDirs().resolve(resolveJsonFileName.apply(Utils.formatFileName(path)));
						  Map.Entry<Path, PropertyCacheModel> entryModel = Map.entry(Values.getCacheDirs().resolve(jsonFilePath), new PropertyCacheModel(path)); 
						  return entryModel;
					  })
					  .forEach(entry -> {
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
