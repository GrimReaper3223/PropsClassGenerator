package com.dsl.classgen.io.sync_refs;

import com.dsl.classgen.io.cache_manager.CacheModel;

sealed interface SyncOperations permits SyncBin, SyncSource {

	boolean insertClassSection(CacheModel model);
	boolean insertFieldSection(CacheModel model);
	void eraseClassSection(CacheModel model);
	boolean eraseFielSection(CacheModel model);
	boolean modifyClassSection(CacheModel model);
	boolean modifyFieldSection(CacheModel model);
}
