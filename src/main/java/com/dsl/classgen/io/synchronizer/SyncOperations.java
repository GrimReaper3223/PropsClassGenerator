package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.dsl.classgen.generator.NewInnerFieldGenerator;
import com.dsl.classgen.generator.NewInnerStaticClassGenerator;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.utils.Utils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

sealed interface SyncOperations permits SyncBin, SyncSource {

	final NewInnerStaticClassGenerator innerClassGen = new NewInnerStaticClassGenerator();
	final NewInnerFieldGenerator innerFieldGen = new NewInnerFieldGenerator();
	
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
