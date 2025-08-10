package com.dsl.classgen.context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

/**
 * The Class PathsContext.
 */
public class PathsContext {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(PathsContext.class);

	/** The Constant SUCCESS level. */
	private static final Level SUCCESS = Level.getLevel("SUCCESS");

	/**
	 * The changed files. Stores information about files that have been updated by
	 * the implementation of the directory monitoring service.
	 */
	private final SynchronousQueue<Map.Entry<Path, WatchEvent.Kind<Path>>> changedFiles;

	/** The properties file list to process. */
	private final List<Path> fileList;

	/** The dir list to watch. */
	private final List<Path> dirList;

	/** The outter class name. */
	private final String outterClassName;

	/** The user reference dir. */
	private final Path userDir;

	/** The cache dir. */
	private final Path cacheDir;

	/** The input properties path. */
	private Path inputPropertiesPath;

	/**
	 * The existing P java generated source path. Save the path of the already
	 * generated source file
	 */
	private Path existingPJavaGeneratedSourcePath;

	/**
	 * The output source dir path. Path where the "generated" package should be
	 * created
	 */
	private Path outputSourceDirPath;

	/**
	 * The output source file path. Path where the source file "P.java" should be
	 * created. This path is resolved with "outputSourceDirPath"
	 */
	private Path outputSourceFilePath;

	/**
	 * The output class file path. Path indicating where the P.class file should be
	 * found, resolved with the existing package path
	 */
	private Path outputClassFilePath;

	/**
	 * The properties data type. The data type found in the properties file matching
	 * the pattern # $javatype:@<java_data_type>
	 */
	private String propertiesDataType;

	/**
	 * The properties file name. Name of the properties file with the extension
	 * .properties
	 */
	private Path propertiesFileName;

	/**
	 * The package class. Used to define the package of the generated class in the
	 * source code
	 */
	private String packageClass;

	/**
	 * Contains the generated class (if this is the first generation). This variable
	 * should be used by the writer to write data to the output path.
	 */
	private String generatedClass;

	/**
	 * Instantiates a new paths context.
	 *
	 * @param isDebugMode the is debug mode
	 */
	PathsContext(boolean isDebugMode) {
		changedFiles = new SynchronousQueue<>();
		fileList = new ArrayList<>();
		dirList = new ArrayList<>();

		outterClassName = "P";
		userDir = Paths.get(System.getProperty("user.dir"));
		outputSourceDirPath = userDir.resolve(isDebugMode ? "src/test/java" : "src/main/java");
		outputClassFilePath = Path.of("target").resolve(isDebugMode ? "test-classes" : "classes");
		cacheDir = userDir.resolve(".jsonProperties-cache");
	}

	/**
	 * Resolves paths during the first generation of files after a certain framework
	 * initialization phase
	 *
	 * @param packageClass the package class
	 */
	public void resolvePaths(String packageClass) {
		var flagsCtx = GeneralContext.getInstance().getFlagsContextInstance();
		if (!flagsCtx.getIsDirStructureAlreadyGenerated()) {
			outputSourceDirPath = outputSourceDirPath.resolve(Utils.normalizePath(packageClass, ".", "/"));
			outputSourceFilePath = outputSourceDirPath.resolve(outterClassName + ".java");
		}
	}

	/**
	 * Gets the file list.
	 *
	 * @return the file list
	 */
	public List<Path> getFileList() {
		return fileList;
	}

	/**
	 * Gets the dir list.
	 *
	 * @return the dir list
	 */
	public List<Path> getDirList() {
		return dirList;
	}

	/**
	 * Queue file.
	 *
	 * @param filePath the file path
	 */
	public void queueFile(Path filePath) {
		fileList.add(filePath);
		LOGGER.log(SUCCESS, "Properties file added to file list: {}", filePath);
	}

	/**
	 * Queue dir.
	 *
	 * @param dirPath the dir path
	 */
	public void queueDir(Path dirPath) {
		WatchServiceImpl.analysePropertyDir(dirPath);
		dirList.add(dirPath);
		LOGGER.log(SUCCESS, "Directory added to dir list: {}", dirPath);
	}

	/**
	 * Check file in cache. If the file to be analyzed has an invalid cache or does
	 * not exist, then this file will be added to a buffer of pending files for
	 * cache writing.
	 *
	 * @param filePath the file path
	 */
	public void checkFileInCache(Path filePath) {
		var flagsCtx = GeneralContext.getInstance().getFlagsContextInstance();
		if (flagsCtx.getIsDirStructureAlreadyGenerated() && flagsCtx.getIsExistsPJavaSource()) {
			if (CacheManager.isInvalidCacheFile(filePath)) {
				CacheManager.queueNewCacheFile(filePath);
			}
		} else {
			CacheManager.queueNewCacheFile(filePath);
		}
	}

	/**
	 * Queue changed file entry.
	 * 
	 * @param entry the entry
	 * @throws InterruptedException the interrupted exception
	 */
	public void queueChangedFileEntry(Map.Entry<Path, WatchEvent.Kind<Path>> entry) throws InterruptedException {
		changedFiles.put(entry);
	}

	/**
	 * Gets the queued changed files entries.
	 *
	 * @return the queued changed files entries
	 * @throws InterruptedException if the wait for new entries of changed files is
	 *                              interrupted
	 */
	public Map.Entry<Path, WatchEvent.Kind<Path>> getQueuedChangedFilesEntries() throws InterruptedException {
		return changedFiles.take();
	}

	/**
	 * Gets the input properties path.
	 *
	 * @return the inputPropertiesPath
	 */
	public Path getInputPropertiesPath() {
		return inputPropertiesPath;
	}

	/**
	 * Sets the input properties path.
	 *
	 * @param inputPropertiesPath the inputPropertiesPath to set
	 */
	public void setInputPropertiesPath(Path inputPropertiesPath) {
		this.inputPropertiesPath = inputPropertiesPath;
	}

	/**
	 * Gets the existing P java generated source path.
	 *
	 * @return the existingPJavaGeneratedSourcePath
	 */
	public Path getExistingPJavaGeneratedSourcePath() {
		return existingPJavaGeneratedSourcePath;
	}

	/**
	 * Sets the existing P java generated source path.
	 *
	 * @param existingPJavaGeneratedSourcePath the existingPJavaGeneratedSourcePath
	 *                                         to set
	 */
	public void setExistingPJavaGeneratedSourcePath(Path existingPJavaGeneratedSourcePath) {
		this.existingPJavaGeneratedSourcePath = existingPJavaGeneratedSourcePath;
	}

	/**
	 * Gets the output source dir path.
	 *
	 * @return the outputSourceDirPath
	 */
	public Path getOutputSourceDirPath() {
		return outputSourceDirPath;
	}

	/**
	 * Sets the output source dir path.
	 *
	 * @param outputSourceDirPath the outputSourceDirPath to set
	 */
	public void setOutputSourceDirPath(Path outputSourceDirPath) {
		this.outputSourceDirPath = outputSourceDirPath;
	}

	/**
	 * Gets the output source file path.
	 *
	 * @return the outputSourceFilePath
	 */
	public Path getOutputSourceFilePath() {
		return outputSourceFilePath;
	}

	/**
	 * Gets the output class file path.
	 *
	 * @return the outputClassFilePath
	 */
	public Path getOutputClassFilePath() {
		return outputClassFilePath;
	}

	/**
	 * Sets the output class file path.
	 *
	 * @param outputClassFilePath the outputClassFilePath to set
	 */
	public void setOutputClassFilePath(Path outputClassFilePath) {
		this.outputClassFilePath = outputClassFilePath;
	}

	/**
	 * Gets the outter class name.
	 *
	 * @return the outterClassName
	 */
	public String getOutterClassName() {
		return outterClassName;
	}

	/**
	 * Gets the user dir.
	 *
	 * @return the userDir
	 */
	public Path getUserDir() {
		return userDir;
	}

	/**
	 * Gets the cache dir.
	 *
	 * @return the cacheDir
	 */
	public Path getCacheDir() {
		return cacheDir;
	}

	/**
	 * Gets the properties data type.
	 *
	 * @return the propertiesDataType
	 */
	public String getPropertiesDataType() {
		return propertiesDataType;
	}

	/**
	 * Sets the properties data type.
	 *
	 * @param propertiesDataType the propertiesDataType to set
	 */
	public void setPropertiesDataType(String propertiesDataType) {
		this.propertiesDataType = propertiesDataType;
	}

	/**
	 * Gets the properties file name.
	 *
	 * @return the propertiesFileName
	 */
	public Path getPropertiesFileName() {
		return propertiesFileName;
	}

	/**
	 * Sets the properties file name.
	 *
	 * @param propertiesFileName the propertiesFileName to set
	 */
	public void setPropertiesFileName(Path propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	/**
	 * Gets the package class.
	 *
	 * @return the packageClass
	 */
	public String getPackageClass() {
		return packageClass;
	}

	/**
	 * Sets the package class.
	 *
	 * @param packageClass the packageClass to set
	 */
	public void setPackageClass(String packageClass) {
		this.packageClass = packageClass;
	}

	/**
	 * Gets the generated class.
	 *
	 * @return the generatedClass
	 */
	public String getGeneratedClass() {
		return generatedClass;
	}

	/**
	 * Sets the generated class.
	 *
	 * @param generatedClass the generatedClass to set
	 */
	public void setGeneratedClass(String generatedClass) {
		this.generatedClass = generatedClass;
	}

	/**
	 * Gets the full package class.
	 *
	 * @return the full package class
	 */
	public String getFullPackageClass() {
		return packageClass + "." + outterClassName + ".java";
	}
}
