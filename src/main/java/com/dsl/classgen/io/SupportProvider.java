package com.dsl.classgen.io;

import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncSource;

public abstract sealed class SupportProvider permits CacheManager, Compiler, Reader, Writer, FileEventsProcessor, FileVisitorImpls, StructureChecker, ChunkLoader {

	protected static final Logger LOGGER = LogManager.getLogger(SupportProvider.class);

	protected static GeneralContext generalCtx = GeneralContext.getInstance();
	protected static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	protected static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	protected SyncSource syncSource = pathsCtx.getExistingPJavaGeneratedSourcePath() != null ? new SyncSource() : null;
	protected SyncBin syncBin = pathsCtx.getOutputClassFilePath() != null && Files.isRegularFile(pathsCtx.getOutputClassFilePath()) ? new SyncBin() : null;

	protected SupportProvider() {}
}
