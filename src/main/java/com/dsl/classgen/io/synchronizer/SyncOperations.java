package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.InnerFieldGenerator;
import com.dsl.classgen.generator.InnerStaticClassGenerator;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.utils.Utils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * The Interface SyncOperations.
 */
sealed interface SyncOperations permits SyncBin, SyncSource {

	Logger LOGGER = LogManager.getLogger(SyncOperations.class);

	GeneralContext generalCtx = GeneralContext.getInstance();
	FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();

	/**
	 * Insert class section.
	 *
	 * @param pathList the path list to process
	 */
	void insertClassSection(Set<Path> pathSet);

	/**
	 * Erase class section.
	 *
	 * @param currentCacheModelList the current cache model list to process
	 */
	void eraseClassSection(Set<CacheModel> currentCacheModelSet);

	/**
	 * Modify section.
	 *
	 * @param mappedChanges     the mapped changes to apply
	 * @param currentCacheModel the current cache model representing the state
	 */
	void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges, CacheModel currentCacheModel);

	/**
	 * Gets the new compilation unit.
	 *
	 * @param path the path to the class file
	 * @return the new compilation unit or null if an error occurs
	 */
	default CompilationUnit getNewCompilationUnit(@NonNull Path path) {
		Objects.requireNonNull(path, "Path cannot be null");
		CompilationUnit cUnit = null;
		try {
			cUnit = StaticJavaParser.parse(path);
		} catch (IOException e) {
			Utils.handleException(e);
		}
		return cUnit;
	}
}
