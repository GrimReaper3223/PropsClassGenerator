package com.dsl.classgen.io.sync_refs;

sealed interface SyncOperations permits SyncBin, SyncSource {

	boolean insertSection();
	boolean eraseSection();
	boolean modifySection();
}
