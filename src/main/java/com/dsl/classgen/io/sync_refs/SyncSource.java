package com.dsl.classgen.io.sync_refs;

import java.nio.file.Path;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;

public final class SyncSource implements SyncOperations {

	private final CacheModel cm;
	private final Path jsonFilePath;
	
	public SyncSource(Path jsonFilePath) {
		this.cm = CacheManager.getElementFromCacheModelMap(Utils.resolveJsonFilePath(jsonFilePath.getFileName()));
		this.jsonFilePath = jsonFilePath;
	}

	@Override
	public boolean insertSection() {
		return false;
	}

	@Override
	public boolean eraseSection() {
		return false;
	}

	@Override
	public boolean modifySection() {
		return false;
	}
}
