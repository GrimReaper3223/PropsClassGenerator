package com.dsl.classgen;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.NewOutterClassGenerator;
import com.dsl.classgen.io.FileEventsProcessor;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.io.synchronizer.BootSync;
import com.dsl.classgen.models.ChunkLoader;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {
	
	private static final Logger LOGGER = LogManager.getLogger(Generator.class);
	
	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	
	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		new GeneratedStructureChecker().checkFileSystem();
		
		flagsCtx.setIsRecursive(isRecursive);
		pathsCtx.setPackageClass(Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString());
		pathsCtx.resolvePaths(pathsCtx.getPackageClass());
		pathsCtx.setInputPropertiesPath(inputPath);
		
		Reader.read(inputPath);
		CacheManager.processCache();
		new ChunkLoader().loadChunks();
		new BootSync().resync();
		
		LOGGER.info("""
				
				-----------------------------
				--- Framework Initialized ---
				-----------------------------
				
				Input Path: {};
				Output Directory Path: {};
				Package Class: {};
				Is Recursive?: {};
				Is Single File?: {};
				Is There a Generated Structure?: {};
				Is there a compiled class?: {};
				
				Developer Options
				Is Debug Mode?: {};
				
				-----------------------------
				-----------------------------
				
				Call 'Generator.generate()' to generate java classes or parse existing classes.
				
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
			new NewOutterClassGenerator().generateData();
			
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
