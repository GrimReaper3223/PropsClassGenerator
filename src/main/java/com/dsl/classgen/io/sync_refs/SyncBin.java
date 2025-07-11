package com.dsl.classgen.io.sync_refs;

import com.dsl.classgen.io.cache_manager.CacheModel;

public final class SyncBin implements SyncOperations {

	@Override
	public boolean insertClassSection(CacheModel model) {
		return false;
	}

	@Override
	public boolean insertFieldSection(CacheModel model) {
		return false;
	}

	@Override
	public void eraseClassSection(CacheModel model) {
	}

	@Override
	public boolean eraseFielSection(CacheModel model) {
		return false;
	}

	@Override
	public boolean modifyClassSection(CacheModel model) {
		return false;
	}

	@Override
	public boolean modifyFieldSection(CacheModel model) {
		return false;
	}

}
