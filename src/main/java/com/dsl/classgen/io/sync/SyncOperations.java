package com.dsl.classgen.io.sync;

import java.nio.file.Path;

import com.dsl.classgen.generator.InnerFieldGenerator;
import com.dsl.classgen.generator.InnerStaticClassGenerator;
import com.dsl.classgen.io.cache_manager.CacheModel;

sealed interface SyncOperations permits SyncBin, SyncSource{

	final InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	final InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();
	
	void insertClassSection(Path path);
	void eraseClassSection(CacheModel model);
	void modifySection(CacheModel model);
}
