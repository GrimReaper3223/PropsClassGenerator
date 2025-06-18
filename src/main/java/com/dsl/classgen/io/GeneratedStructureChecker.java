package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public class GeneratedStructureChecker {

	// verifica se existe a estrutura gerada pelo framework
	// false pode ser lancado caso nao exista o diretorio /generated
	// ou caso nao exista o arquivo P.java
	public static void checkGeneratedStructure() {
		try {
			Values.setIfFileStructureAlreadyGenerated(Utils.getExecutor().submit(() -> {
				try (Stream<Path> dirs = constructDirStreamFind()) {
					return dirs.filter(path -> path.toString().contains("generated"))
							   .findFirst()
							   .map(GeneratedStructureChecker::isExistsJavaFile)
							   .orElse(false);
				}
			}).get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkIfExistsCompiledClass() {
		Path fullCompilationPath = Values.getCompilationPath().resolve(Values.getOutputFilePath());
		Values.setIfExistsCompiledPJavaClass(Files.exists(fullCompilationPath));
	}
	
	// constroi um stream de busca de diretorios
	private static Stream<Path> constructDirStreamFind() {
		Stream<Path> dirs = null;
		try {
			dirs = Files.find(Values.getOutputPackagePath(), 
						   Short.MAX_VALUE, 
						   (path, _) -> Files.isDirectory(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dirs;
	}
	
	// verifica se existe o arquivo java gerado pelo framework
	private static boolean isExistsJavaFile(Path path) {
		boolean isExistsJavaFile = false;
		try(Stream<Path> files = Files.list(path)) {
			isExistsJavaFile = files.map(p -> {
				boolean val = p.getFileName().toString().equals(Values.getOutterClassName() + ".java");
				if (val) {
					// extrai o caminho que contem a classe principal de anotacoes para a variavel de input
					Values.setExistingPJavaGeneratedSourcePath(p);
					Values.setIfExistsPJavaGeneratedSourcePath(true);
				}
				return val;
			})
			.findFirst()
			.isPresent();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isExistsJavaFile;  
	}
}
