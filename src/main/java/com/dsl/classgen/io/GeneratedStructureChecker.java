package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

/**
 * The Class GeneratedStructureChecker.
 */
public final class GeneratedStructureChecker extends SupportProvider {

	private Function<String, Predicate<Path>> predicateFactory = str -> p -> p.getFileName().toString().equals(str);
	private BiFunction<Stream<Path>, Predicate<Path>, Path> genFilter = (pathStream, predicate) -> pathStream
			.filter(predicate::test).findFirst().orElse(null);

	/**
	 * Check file system.
	 */
	public void checkFileSystem() {
		LOGGER.info("Analyzing the file system...");
		checkDirGeneratedStructure();
		checkIfExistsCompiledClass();
		checkIfIsExistsSourceFile();
	}

	/**
	 * Check dir generated structure.
	 */
	private void checkDirGeneratedStructure() {
		try (Stream<Path> dirs = Files.find(pathsCtx.getOutputSourceDirPath(), Integer.MAX_VALUE,
				(path, _) -> Files.isDirectory(path))) {
			Path foundedPath = genFilter.apply(dirs, predicateFactory.apply("generated"));
			if (foundedPath != null) {
				pathsCtx.setOutputSourceDirPath(foundedPath);
				flagsCtx.setIsDirStructureAlreadyGenerated(true);
			}
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Check if exists compiled class.
	 */
	private void checkIfExistsCompiledClass() {
		try (Stream<Path> files = Files.find(pathsCtx.getOutputClassFilePath(), Integer.MAX_VALUE,
				(path, _) -> Files.isRegularFile(path))) {
			Path foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".class"));
			if (foundedPath != null) {
				pathsCtx.setOutputClassFilePath(foundedPath);
				flagsCtx.setIsExistsCompiledPJavaClass(true);
			}
		} catch (IOException e) {
			Utils.logException(e);
		}
	}

	/**
	 * Check if is exists source file.
	 */
	private void checkIfIsExistsSourceFile() {
		try (Stream<Path> files = Files.list(pathsCtx.getOutputSourceDirPath())) {
			Path foundedPath = genFilter.apply(files, predicateFactory.apply(pathsCtx.getOutterClassName() + ".java"));
			pathsCtx.setExistingPJavaGeneratedSourcePath(foundedPath);
			flagsCtx.setIsExistsPJavaSource(foundedPath != null);
		} catch (IOException e) {
			Utils.logException(e);
		}
	}
}
