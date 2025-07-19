package com.dsl.classgen.io.synchronizer;

import java.nio.file.Path;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheModel;

public final class SyncBin extends SupportProvider implements SyncOperations {

	@Override
	public void insertClassSection(Path path) {
		// document why this method is empty
	}

	@Override
	public void eraseClassSection(CacheModel model) {
		// document why this method is empty
	}

	@Override
	public void modifySection(CacheModel model) {
		// document why this method is empty
	}
}
