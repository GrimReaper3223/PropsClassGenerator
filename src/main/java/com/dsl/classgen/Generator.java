package com.dsl.classgen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.OutterClassGenerator;
import com.dsl.classgen.io.FileEventsProcessor;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.cache_manager.CacheProcessorOption;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;

public final class Generator {
	
	private static final Logger LOGGER = LogManager.getLogger(Generator.class);
	
	private static GeneralContext fwCtx = GeneralContext.getInstance();
	private static FlagsContext flagsCtx = fwCtx.getFlagsContextInstance();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	
	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		new GeneratedStructureChecker().checkFileSystem();
		
		flagsCtx.setIsRecursive(isRecursive);
		pathsCtx.setInputPropertiesPath(inputPath);
		pathsCtx.setPackageClass(Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString());
		pathsCtx.resolvePaths(pathsCtx.getPackageClass());
		
		Reader.read(inputPath);
		
		if(flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource() && flagsCtx.getIsExistsCompiledPJavaClass()) {
			SyncSource syncSource = new SyncSource();
			SyncBin syncBin = new SyncBin();
			
			LOGGER.warn("There is already a generated structure.");
			LOGGER.log(Levels.NOTICE.getLevel(), "Checking files...");
			
			CacheManager.processCache(CacheProcessorOption.LOAD);
			
			// apaga entradas inexistentes do cache
			var filteredModelList = CacheManager.getCacheModelMapEntries()
						.stream()
						.map(entry -> Path.of(entry.getValue().filePath))
						.filter(elem -> !pathsCtx.getFileList().contains(elem))
						.filter(Objects::nonNull)
						.toList();
			
			List<CacheModel> removedModels = new ArrayList<>();
			
			if(!filteredModelList.isEmpty()) {
				LOGGER.warn("Changes detected. Synchronizing entities...");
				removedModels.addAll(filteredModelList.stream()
						 .map(CacheManager::removeElementFromCacheModelMap)
						 .filter(Objects::nonNull)
						 .toList());
			}
			
			if(!removedModels.isEmpty()) {
				syncSource.eraseClassSection(removedModels);
				syncBin.eraseClassSection(removedModels);
			}
	
			if(CacheManager.hasCacheToWrite()) {
				CacheManager.getQueuedCacheFiles(false)
							.stream()
							.forEach(path -> {
								if(Files.exists( Utils.resolveJsonFilePath(path))) {
									var model = CacheManager.getElementFromCacheModelMap(path);
									syncSource.eraseClassSection(model);
									syncSource.insertClassSection(path);
									
									syncBin.eraseClassSection(model);
									syncBin.insertClassSection(path);
								} else {
									syncSource.insertClassSection(path);
									syncBin.insertClassSection(path);
								}
							});
			}
		}
		CacheManager.processCache();
		
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
			new OutterClassGenerator().generateOutterClass();
			
			if(flagsCtx.getIsDebugMode()) {
				LOGGER.debug(pathsCtx.getGeneratedClass());
			} 
			Writer.write();
		}
		
		Compiler.compile();
		WatchServiceImpl.initialize();
		FileEventsProcessor.initialize();
	}
}
