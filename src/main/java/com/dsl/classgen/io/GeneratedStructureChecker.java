package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public class GeneratedStructureChecker {
	
	/*
	 * Verifica se existe a estrutura de diretorios e arquivos utilizaveis 
	 * por este framework.
	 * 
	 * A verificacao comeca buscando um pacote contendo "generated" no nome
	 * Se o pacote for encontrado, sera buscado a classe principal gerada anteriormente pelo framework
	 */
    public static void checkGeneratedStructure() {
        try {
            Values.setIfDirStructureAlreadyGenerated(Utils.getExecutor().submit(() -> 
                GeneratedStructureChecker.findGeneratedDir()
                								.findFirst()
				                    		    .flatMap(GeneratedStructureChecker::isExistsJavaFile)
				                    		    .orElse(false)).get());
            checkIfExistsCompiledClass();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // verifica se a classe compilada existe
    private static void checkIfExistsCompiledClass() {
    	try {
    		Path classFileBinPath = Files.find(Values.getCompilationPath(),
							        		Short.MAX_VALUE, 
							        		(path, _) -> path.getFileName().toString().equals(Values.getOutterClassName() + ".class"))
												.findFirst()
												.orElse(null);
			Values.setOutputClassFilePath(classFileBinPath);
    		Values.setIfExistsCompiledPJavaClass(Objects.nonNull(classFileBinPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    // constroi um fluxo em /src/main/java
    private static Stream<Path> findGeneratedDir() {
        Stream<Path> dirStream = null;
        try {
            dirStream = Files.find(Values.getOutputPackagePath(),
            		Short.MAX_VALUE, 
            		(path, _) -> Files.isDirectory(path))
            			.filter(path -> path.getFileName().toString().equals("generated"))
            			.findFirst()
            			.stream();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return dirStream;
    }

    // verifica se o arquivo P.java existe
    private static Optional<Boolean> isExistsJavaFile(Path path) {
        boolean isExistsJavaFile = false;
        try (Stream<Path> files = Files.list(path)){
            isExistsJavaFile = files.map(p -> {
                boolean val = p.getFileName().toString().equals(Values.getOutterClassName() + ".java");
                if (val) {
                    Values.setExistingPJavaGeneratedSourcePath(p);
                }
                return val;
            })
            .findFirst()
            .isPresent();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.of(isExistsJavaFile);
    }
}

