package com.dsl.classgen.io.synchronizer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.dsl.classgen.generator.InnerFieldGenerator;
import com.dsl.classgen.generator.InnerStaticClassGenerator;
import com.dsl.classgen.io.cache_manager.CacheModel;

sealed interface SyncOperations permits SyncBin, SyncSource {

	final InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	final InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();
	
	void insertClassSection(List<Path> pathList);
	void eraseClassSection(List<CacheModel> currentCacheModelList);
	void modifySection(ModelMapper<Map<String, Integer>> mappedChanges, CacheModel currentCacheModel);
}
