package com.dsl.classgen.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.utils.Utils;

public class Writer {

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
}
