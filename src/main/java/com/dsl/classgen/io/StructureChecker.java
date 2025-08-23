package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public final class StructureChecker extends SupportProvider {

	private static Function<String, Predicate<Path>> predicateFactory = str -> p -> p.getFileName().toString().equals(str);
	private static BiFunction<Stream<Path>, Predicate<Path>, Path> genFilter =
						(pathStream, predicate) -> pathStream.filter(predicate::test)
															 .findFirst()
															 .orElse(null);

	private StructureChecker() {}

	/*
	 * Verifica se existe a estrutura de diretorios e arquivos utilizaveis
	 * por este framework.
	 *
	 * A verificacao comeca buscando um pacote contendo "generated" no nome
	 * Se o pacote for encontrado, sera buscado a classe principal gerada anteriormente pelo framework
	 */
	public static void checkStructure() {
		LOGGER.info("Analyzing the file system...");
		pathsCtx.setOutputSourceDirPath(checkDirStructure());
  		pathsCtx.setExistingPJavaGeneratedSourcePath(checkSourceFile());
  		pathsCtx.setOutputClassFilePath(checkCompiledClass());
	}

    private static Path checkDirStructure() {
    	Path foundedPath = null;
        try (Stream<Path> dirs = Files.find(pathsCtx.getOutputSourceDirPath(), Integer.MAX_VALUE, (path, _) -> Files.isDirectory(path))){
        	foundedPath = genFilter.apply(dirs, predicateFactory.apply("generated"));
        }
        catch (IOException e) {
        	Utils.handleException(e);
        }
        return Objects.requireNonNullElse(foundedPath, pathsCtx.getOutputSourceDirPath());
    }

    // verifica se o arquivo P.java existe
    private static Path checkSourceFile() {
    	Path foundedPath = null;
    	try (Stream<Path> files = Files.list(pathsCtx.getOutputSourceDirPath())) {
    		foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".java"));
    	}
    	catch (IOException e) {
    		Utils.handleException(e);
    	}
    	return foundedPath;
    }

    // verifica se a classe compilada existe
    private static Path checkCompiledClass() {
    	Path foundedPath = null;
    	try(Stream<Path> files = Files.find(pathsCtx.getOutputClassFilePath(), Integer.MAX_VALUE, (path, _) -> Files.isRegularFile(path))) {
			foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".class"));
    	}
    	catch (IOException e) {
    		Utils.handleException(e);
        }
    	return Objects.requireNonNullElse(foundedPath, pathsCtx.getOutputClassFilePath());
    }
}