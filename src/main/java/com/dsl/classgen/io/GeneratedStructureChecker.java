package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class GeneratedStructureChecker extends SupportProvider {

	private Function<String, Predicate<Path>> predicateFactory = str -> p -> p.getFileName().toString().equals(str);
	private BiFunction<Stream<Path>, Predicate<Path>, Path> genFilter = 
						(pathStream, predicate) -> pathStream.filter(predicate::test)
															 .findFirst()
															 .orElse(null);
	
	/*
	 * Verifica se existe a estrutura de diretorios e arquivos utilizaveis 
	 * por este framework.
	 * 
	 * A verificacao comeca buscando um pacote contendo "generated" no nome
	 * Se o pacote for encontrado, sera buscado a classe principal gerada anteriormente pelo framework
	 */
	
	public void checkFileSystem() {
		LOGGER.info("Analyzing the file system...");
		checkDirGeneratedStructure();
		checkIfExistsCompiledClass();
		checkIfIsExistsSourceFile();
	}
	
    private void checkDirGeneratedStructure() {
        try (Stream<Path> dirs = Files.find(pathsCtx.getOutputSourceDirPath(), Integer.MAX_VALUE, (path, _) -> Files.isDirectory(path))){
        	Path foundedPath = genFilter.apply(dirs, predicateFactory.apply("generated"));
        	if(foundedPath != null) {
        		pathsCtx.setOutputSourceDirPath(foundedPath);
        		flagsCtx.setIsDirStructureAlreadyGenerated(true);
        	}
        }
        catch (IOException e) {
            LOGGER.fatal(e);
        }
    }
    
    // verifica se a classe compilada existe
    private void checkIfExistsCompiledClass() {
    	try(Stream<Path> files = Files.find(pathsCtx.getOutputClassFilePath(), Integer.MAX_VALUE, (path, _) -> Files.isRegularFile(path))) {
			Path foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".class"));
			if(foundedPath != null) {
        		pathsCtx.setOutputClassFilePath(foundedPath);
        		flagsCtx.setIsExistsCompiledPJavaClass(true);
        	}
    	}
    	catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    // verifica se o arquivo P.java existe
    private void checkIfIsExistsSourceFile() {
        try (Stream<Path> files = Files.list(pathsCtx.getOutputSourceDirPath())) {
            Path foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".java"));
            pathsCtx.setExistingPJavaGeneratedSourcePath(foundedPath);
            flagsCtx.setIsExistsPJavaSource(foundedPath != null);
        }
        catch (IOException e) {
            LOGGER.fatal(e);
        }
    }
}

