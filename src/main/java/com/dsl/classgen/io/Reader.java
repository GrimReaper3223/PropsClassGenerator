package com.dsl.classgen.io;

import static com.dsl.classgen.io.Values.getIsRecursive;
import static com.dsl.classgen.io.Values.getPathQueue;
import static com.dsl.classgen.io.Values.getProps;
import static com.dsl.classgen.io.Values.setIsSingleFile;
import static com.dsl.classgen.io.Values.setPathQueue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Reader {

	private Reader() {}

	public static void read(Path inputPath) {
		if(Files.isRegularFile(inputPath)) {
			loadPropFile(inputPath);
			setIsSingleFile(true);
			
		} else if(Files.isDirectory(inputPath)) {
			processFileList(inputPath);
		}
	}
	
	// carrega o arquivo de propriedades
	public static void loadPropFile(Path inputPath) {
		var props = getProps();
		
		try(InputStream in = Files.newInputStream(inputPath)) {
			if(!props.isEmpty()) {
				props.clear();
			}
			props.load(in);
			
			processFile(inputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.format("%n%n***Properties file loaded from path: %s***%n", inputPath);
	}
	
	private static void processFile(Path inputPath) {
		try(Stream<String> lines = Files.lines(inputPath, StandardCharsets.ISO_8859_1)) {
			Values.setPropertiesDataType(lines.filter(input -> input.contains("$javatype:"))
									.findFirst()
									.map(input -> input.substring(input.indexOf('@') + 1))
									.orElseThrow(() -> new IOException(Values.getExceptionTxt())));
			
			String fileName = inputPath.getFileName().toString();
			Values.setPropertiesfileName(fileName.substring(0, fileName.lastIndexOf('.')));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void processFileList(Path inputPath) {
		Predicate<Path> testPath = path -> {
			String stringPath = path.getFileName().toString();
			return stringPath.substring(stringPath.indexOf('.')).endsWith(".properties");
		};
		
		try {
			if(getIsRecursive()) {
				Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if(testPath.test(file)) {
							getPathQueue().add(file);
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} else {
				try(Stream<Path> pathStream = Files.list(inputPath)) {
					setPathQueue(pathStream.filter(Files::isRegularFile)
										   .filter(testPath::test)
										   .toList()); 
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			setIsSingleFile(false);
		}
	}
}
