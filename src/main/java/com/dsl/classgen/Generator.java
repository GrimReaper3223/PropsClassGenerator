package com.dsl.classgen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generator.OutterClassGenerator;
import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.io.FileEventsProcessor;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.io.synchronizer.SyncSource;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

/**
 * The Class Generator.
 */
public final class Generator {

	private static final Logger LOGGER = LogManager.getLogger(Generator.class);

	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	private Generator() {}

	/**
	 * Inits the framework with the specified parameters.
	 *
	 * @param inputPath    the path of the property file (or directory containing
	 *                     such files) entered that serves as the entry point for
	 *                     generation
	 * @param packageClass the package where the source file should be generated
	 * @param isRecursive  indicates whether generation should recursively search
	 *                     for property files to map them. Note that if only a file
	 *                     is passed and not a directory, recursion will not work.
	 */
	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		new GeneratedStructureChecker().checkFileSystem();

		flagsCtx.setIsRecursive(isRecursive);
		pathsCtx.setPackageClass(Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString());
		pathsCtx.resolvePaths(pathsCtx.getPackageClass());
		pathsCtx.setInputPropertiesPath(inputPath);

		Reader.read(inputPath);
		CacheManager.processCache();
		loadChunks();
		resync();

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

	/**
	 * @see #init(Path, String, boolean)
	 * @author Deiv
	 */
	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}

	/**
	 * Starts generating all necessary files
	 *
	 * @since
	 * @author Deiv
	 */
	public static void generate() {
		if (!flagsCtx.getIsDirStructureAlreadyGenerated() || !flagsCtx.getIsExistsPJavaSource()) {
			Utils.calculateElapsedTime();
			new OutterClassGenerator().generateData();

			if (flagsCtx.getIsDebugMode()) {
				LOGGER.debug(pathsCtx.getGeneratedClass());
			}
			Writer.writeFirstGeneration();
		}

		Compiler.compile();
		WatchServiceImpl.initialize();
		FileEventsProcessor.initialize();
	}

	/**
	 * Loads chunks (structural models mapped from property files) into memory
	 *
	 * @author Deiv
	 */
	private static void loadChunks() {
		LOGGER.info("Loading chunks...");
		pathsCtx.getFileList().forEach(path -> {
			InnerStaticClassModel.initInstance(path);
			pathsCtx.checkFileInCache(path);
		});
	}

	/**
	 * It is called when the framework starts. It should only be called once.
	 *
	 * @author Deiv
	 */
	private static void resync() {
		if (flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource()
				&& flagsCtx.getIsExistsCompiledPJavaClass()) {
			LOGGER.warn("There is already a generated structure.");
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Looking for changes...");

			List<CacheModel> outdatedCache = filterDeletedFiles();

			if (!outdatedCache.isEmpty()) {
				LOGGER.warn("Changes detected. Synchronizing entities...");
				new SyncSource().eraseClassSection(outdatedCache);
				new SyncBin().eraseClassSection(outdatedCache);

			} else {
				LOGGER.warn("All is up to date.");
			}
			writeNewCacheIfExists();
		}
	}

	/**
	 * Filters files deleted while the framework was not in operation
	 *
	 * @return a list of CacheModel objects representing the deleted files
	 * @author Deiv
	 */
	private static List<CacheModel> filterDeletedFiles() {
		return CacheManager.getCacheModelMapEntries()
				.stream()
				.filter(entry -> !OutterClassModel.checkPathInClassModelMap(entry.getValue().filePath))
				.map(CacheManager::removeElementFromCacheModelMap)
				.filter(Objects::nonNull)
				.toList();
	}

	/**
	 * Writes the new cache to disk if there are any new files to write.
	 *
	 * @author Deiv
	 */
	private static void writeNewCacheIfExists() {
		if (CacheManager.hasCacheToWrite()) {
			LOGGER.warn("New cache to write. Synchronizing entities...");
			CacheManager.getQueuedCacheFiles(false).stream().forEach(path -> {
				WatchEvent.Kind<Path> event = Files.exists(Utils.resolveJsonFilePath(path))
						? StandardWatchEventKinds.ENTRY_MODIFY
						: StandardWatchEventKinds.ENTRY_CREATE;
				FileEventsProcessor.caller(event, path);
			});
		}
	}
}
