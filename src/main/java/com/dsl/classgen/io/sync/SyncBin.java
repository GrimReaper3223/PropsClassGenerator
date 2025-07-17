package com.dsl.classgen.io.sync;

import java.nio.file.Path;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheModel;

public final class SyncBin extends SupportProvider implements SyncOperations {

	@Override
	public void insertClassSection(Path path) {
	}

	@Override
	public void eraseClassSection(CacheModel model) {
	}

	@Override
	public void modifySection(CacheModel model) {
	}
}
