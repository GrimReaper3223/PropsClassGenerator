package com.dsl.classgen;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.OutterClassGenerator;
import com.dsl.classgen.io.ChunkLoader;
import com.dsl.classgen.io.FileEventsProcessor;
import com.dsl.classgen.io.StructureChecker;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {

	private static final Logger LOGGER = LogManager.getLogger(Generator.class);

	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		flagsCtx.setRecursion(isRecursive);
		pathsCtx.setPackageClass(packageClass);
		pathsCtx.setInputPath(inputPath);

		StructureChecker.checkStructure();
//		pathsCtx.resolvePaths();

		Reader.read(inputPath);
		ChunkLoader.loadChunks();

		LOGGER.info("""

				-----------------------------
				--- Framework Initialized ---
				-----------------------------

				Input Path: {};
				Output Path: {};
				Package: {};
				Is Recursive?: {};
				Is Single File?: {};
				Is There a Generated Structure?: {};
				Is There a Compiled Class?: {};

				Dev Options:
				Is Debug Mode?: {};

				-----------------------------
				-----------------------------

				Call 'Generator.generate()' to generate java classes or manage existing classes.
				""", inputPath,
					 pathsCtx.getOutputSourceDirPath(),
					 pathsCtx.getPackageClass(),
					 isRecursive,
					 flagsCtx.getIsSingleFile(),
					 flagsCtx.getIsDirStructureAlreadyGenerated(),
					 flagsCtx.getIsExistsCompiledPJavaClass(),
					 flagsCtx.getIsDebugMode());
	}

	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}

	public static void generate() {
		if (!flagsCtx.getIsDirStructureAlreadyGenerated() || !flagsCtx.getIsExistsPJavaSource()) {
			Utils.calculateElapsedTime();
			new OutterClassGenerator().generateData();

			if(flagsCtx.getIsDebugMode()) {
				LOGGER.debug(pathsCtx.getGeneratedClass());
			}
			Writer.writeFirstGeneration();
		}

		Compiler.compile();
		WatchServiceImpl.initialize();
		FileEventsProcessor.initialize();
	}
}
