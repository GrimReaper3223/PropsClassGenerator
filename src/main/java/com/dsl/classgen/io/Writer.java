package com.dsl.classgen.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dsl.classgen.utils.Utils;

public class Writer {

	private Writer() {}
	
	public static void write() throws IOException {
		System.out.println("\n\nWriting data...");
		Path outputPath = Values.getOutputPath();
		
		if(Files.notExists(outputPath)) {
			Files.createDirectories(outputPath);
			Path filePath = Files.createFile(outputPath.resolve(Path.of(Values.getOutterClassName().concat(".java"))));
			try(OutputStream out = Files.newOutputStream(filePath)) {
				out.write(Values.getGeneratedClass().getBytes());
			}
		}
		System.out.format("%nFile created in: %s [Elapsed Time: %dms]", outputPath, Utils.calculateElapsedTime());
	}
}
