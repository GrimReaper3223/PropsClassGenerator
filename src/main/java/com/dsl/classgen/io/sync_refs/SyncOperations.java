package com.dsl.classgen.io.sync_refs;

import java.nio.file.Path;

import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generators.InnerFieldGenerator;
import com.dsl.classgen.generators.InnerStaticClassGenerator;
import com.dsl.classgen.io.cache_manager.CacheModel;

sealed interface SyncOperations permits SyncBin, SyncSource {

	final FrameworkContext fwCtx = FrameworkContext.get();
	final PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	
	final InnerStaticClassGenerator innerClassGen = new InnerStaticClassGenerator();
	final InnerFieldGenerator innerFieldGen = new InnerFieldGenerator();
	
	void insertClassSection(Path path);
	void eraseClassSection(CacheModel model);
	void modifyFieldSection(CacheModel model);
}
