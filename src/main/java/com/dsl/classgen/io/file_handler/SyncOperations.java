package com.dsl.classgen.io.file_handler;

sealed interface SyncOperations permits SyncBin, SyncSource {

	boolean insertSection();
	boolean eraseSection();
	boolean modifySection();
}
