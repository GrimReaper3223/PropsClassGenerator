package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

sealed interface SyncOperations permits SyncBin, SyncSource {

	final InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	final InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();
	
	final Logger LOGGER = LogManager.getLogger(SyncOperations.class);
	
	final GeneralContext generalCtx = GeneralContext.getInstance();
	final FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	final PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	
	void insertClassSection(List<Path> pathList);
	void eraseClassSection(List<CacheModel> currentCacheModelList);
	void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges, CacheModel currentCacheModel);
	
	default CompilationUnit getNewCompilationUnit(Path path) {
		Objects.requireNonNull(path, "Path cannot be null");
		CompilationUnit cUnit = null;
		try {
			cUnit = StaticJavaParser.parse(path);
		} catch (IOException e) {
			Utils.logException(e);
		}
		return cUnit;
	}
}
