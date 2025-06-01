package com.dsl.classgen.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dsl.classgen.Generator;

public class OutputWriter {

	private OutputWriter() {}
	
	public static void write() throws IOException {
		Path outputPath = Generator.getOutputPath();
		String outputPackage = Generator.getPackageOfGeneratedClass().concat(".generated");
		String fileName = Generator.getOutterClassName().concat(".java");
		Path path = outputPath.resolve(Path.of(outputPackage.replaceAll("[.]", "/")));
		
		if(Files.notExists(path)) {
			Files.createDirectories(path);
			Path filePath = Files.createFile(path.resolve(Path.of(fileName)));
			try(OutputStream out = Files.newOutputStream(filePath)) {
				out.write(Generator.getGeneratedClass().getBytes());
			}
		}
		System.out.format("%nFile created in: %s [Elapsed Time: %dms]", path, Generator.calculateElapsedTime());
	}
}
