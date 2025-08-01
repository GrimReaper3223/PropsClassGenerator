package com.dsl.classgen.io.synchronizer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.dsl.classgen.generator.InnerFieldGenerator;
import com.dsl.classgen.generator.InnerStaticClassGenerator;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;

sealed interface SyncOperations permits SyncBin, SyncSource {

	final InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	final InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();
	
	void insertClassSection(List<Path> pathList);
	void eraseClassSection(List<CacheModel> currentCacheModelList);
	void modifySection(ModelMapper<Map<Integer, CachePropertiesData>> mappedChanges, CacheModel currentCacheModel);
}
