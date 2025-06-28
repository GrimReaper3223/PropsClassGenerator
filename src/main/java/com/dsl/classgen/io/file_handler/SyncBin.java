package com.dsl.classgen.io.file_handler;

import java.nio.file.Path;

import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.cache_system.HashTableModel;
import com.dsl.classgen.utils.Utils;

public final class SyncBin implements SyncOperations {

	private final HashTableModel htm;
	private final Path jsonFilePath;
	
	public SyncBin(HashTableModel htm, Path jsonFilePath) {
		this.htm = Values.getElementFromHashTableMap(Utils.resolveJsonFilePath(jsonFilePath.getFileName()));;
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
