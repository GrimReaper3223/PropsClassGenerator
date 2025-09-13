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

	public static <T> void init(T inputPath, String packageClass, boolean isRecursive) {
		Path path = Path.of(inputPath.toString());
		if(!flagsCtx.isItAlreadyRunning()) {
    		flagsCtx.setRecursion(isRecursive);
    		pathsCtx.setPackageClass(packageClass);
    		pathsCtx.setInputPath(path);

    		StructureChecker.checkStructure();

    		Reader.read(path);
    		ChunkLoader.loadChunks();

    		LOGGER.info("""

				-----------------------------
				--- Framework Initialized ---
				-----------------------------

				Input Path: {};
				Output Path: {};
				Package: {};
				Is Recursive?: {};
				Is There a Generated Structure?: {};
				Is There a Compiled Class?: {};

				Dev Options:
				Is Debug Mode?: {};

				-----------------------------
				-----------------------------

				Call 'Generator.generate()' to generate java classes or manage existing classes.
				""", path,
					 pathsCtx.getOutputSourceDirPath(),
					 pathsCtx.getPackageClass(),
					 isRecursive,
					 flagsCtx.hasSourceStructureGenerated(false),
					 flagsCtx.isExistsCompiledPJavaClass(),
					 flagsCtx.isDebugMode());
		}
	}

	public static void generate() {
		if(!flagsCtx.isItAlreadyRunning()) {
    		if (!flagsCtx.hasSourceStructureGenerated(true)) {
    			Utils.calculateElapsedTime();
    			new OutterClassGenerator().generateData();

    			if(flagsCtx.isDebugMode()) {
    				LOGGER.debug(pathsCtx.getGeneratedClass());
    			}
    			Writer.writeFirstGeneration();
    		}

    		Compiler.compile();
    		WatchServiceImpl.initialize();
    		FileEventsProcessor.initialize();
		}
	}
}
