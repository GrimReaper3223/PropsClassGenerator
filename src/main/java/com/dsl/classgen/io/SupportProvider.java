package com.dsl.classgen.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.io.sync.SyncBin;
import com.dsl.classgen.io.sync.SyncSource;
import com.dsl.classgen.utils.Levels;

public abstract sealed class SupportProvider implements Levels permits CacheManager, Compiler, Reader, Writer, SyncSource, FileEventsProcessor, SyncBin, FileVisitorImpls, GeneratedStructureChecker, CacheModel {

	protected static final Logger LOGGER = LogManager.getLogger(SupportProvider.class);
	
	protected static GeneralContext generalCtx = GeneralContext.get();
	protected static FlagsContext flagsCtx = generalCtx.getFlagsInstance();
	protected static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	
	protected SupportProvider() {}
}
