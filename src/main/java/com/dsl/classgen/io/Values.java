package com.dsl.classgen.io;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.gson.Gson;

public class Values {

	// deve ser true durante o desenvolvimento
	private static boolean isDebugMode = false;
	
	private static final Properties PROPS = new Properties();
	private static final Gson GSON = new Gson();
	private static final String OUTTER_CLASS_NAME = "P";
	
	private static List<Path> fileList = new ArrayList<>();
	private static List<Path> dirList = new ArrayList<>();
	
	private static BlockingQueue<Map.Entry<Path, ?>> changedFile = new ArrayBlockingQueue<>(128);
	
	private static boolean isSingleFile;
	private static boolean isRecursive;				
	private static boolean hasStructureAlreadyGenerated;
	
	private static String propertiesDataType;
	
	private static String propertiesfileName;
	private static String packageClass;				
	private static String generatedClass;
	private static Path outputPath = Path.of("src", "main", "java");
	private static Path inputPath;
	private static Path existingPath;
	
	private static final Path CACHE_DIRS = Path.of(System.getProperty("user.dir"), ".jsonProperties-cache");
	private static final String JSON_FILENAME_PATTERN = "%s-cache.json";	// <nome_do_arquivo>-cache.json
	
	private static long startTimeOperation = 0L;
	private static long endTimeOperation = 0L;
	
	private static final String EXCEPTION_TXT = """
			Error: The variable type identification was not found for creating the classes.
			Enter the variable type and try again.
			Preferably, place the identifier at the top of the file so that it can be analyzed faster.
			
			Ex.:
			# $javatype:@String
			
			... rest of the .properties file...
			
			# - Comment for the property file;
			$javatype - Java type identifier;
			: - Syntactic separator;
			@ - Tells the service that after this identifier, the java type will be read for the variable;
			String - The java type used in this example;
			""";
	
	public static void resolvePaths() {
		packageClass = packageClass.concat(".generated");
		outputPath = outputPath.resolve(Path.of(packageClass.replaceAll("[.]", "/")));
	}
	
	public static <T extends Kind<?>> void addChangedValueToMap(Entry<Path, T> entry) {
		changedFile.add(entry);
	}
	
	/**
	 * @return the pathQueue
	 */
	public static List<Path> getDirList() {
		return dirList;
	}
	
	/**
	 * @param dirPath the dirPath to add
	 */
	public static void addDirToList(Path dirPath) {
		dirList.add(dirPath);
	}
	
	/**
	 * @return the pathQueue
	 */
	public static List<Path> getFileList() {
		return fileList;
	}
	
	/**
	 * @param filePath the filePath to set
	 */
	public static void addFileToList(Path filePath) {
		fileList.add(filePath);
	}

	/**
	 * @param fileList the fileList to set
	 */
	public static void setFileList(List<Path> fileList) {
		Values.fileList = fileList;
	}

	public static boolean hasStructureAlreadyGenerated() {
		return hasStructureAlreadyGenerated;
	}

	public static void setHasStructureAlreadyGenerated(boolean hasStructureAlreadyGenerated) {
		Values.hasStructureAlreadyGenerated = hasStructureAlreadyGenerated;
	}
	
	/**
	 * @return the isSingleFile
	 */
	public static boolean isSingleFile() {
		return isSingleFile;
	}

	/**
	 * @param isSingleFile the isSingleFile to set
	 */
	public static void setIsSingleFile(boolean isSingleFile) {
		Values.isSingleFile = isSingleFile;
	}

	/**
	 * @return the isRecursive
	 */
	public static boolean isRecursive() {
		return isRecursive;
	}

	/**
	 * @param isRecursive the isRecursive to set
	 */
	public static void setIsRecursive(boolean isRecursive) {
		Values.isRecursive = isRecursive;
	}

	/**
	 * @return the propertiesDataType
	 */
	public static String getPropertiesDataType() {
		return propertiesDataType;
	}

	/**
	 * @param propertiesDataType the propertiesDataType to set
	 */
	public static void setPropertiesDataType(String propertiesDataType) {
		Values.propertiesDataType = propertiesDataType;
	}

	/**
	 * @return the propertiesfileName
	 */
	public static String getPropertiesFileName() {
		return propertiesfileName;
	}

	/**
	 * @param propertiesfileName the propertiesfileName to set
	 */
	public static void setPropertiesfileName(String propertiesfileName) {
		Values.propertiesfileName = propertiesfileName;
	}

	/**
	 * @return the cacheDirs
	 */
	public static Path getCacheDirs() {
		return CACHE_DIRS;
	}

	/**
	 * @return the packageClass
	 */
	public static String getPackageClass() {
		return packageClass;
	}

	/**
	 * @param packageClass the packageClass to set
	 */
	public static void setPackageClass(String packageClass) {
		Values.packageClass = packageClass;
	}

	/**
	 * @return the generatedClass
	 */
	public static String getGeneratedClass() {
		return generatedClass;
	}

	/**
	 * @param generatedClass the generatedClass to set
	 */
	public static void setGeneratedClass(String generatedClass) {
		Values.generatedClass = generatedClass;
	}

	/**
	 * @return the existingPath
	 */
	public static Path getExistingPath() {
		return existingPath;
	}

	/**
	 * @param existingPath the existingPath to set
	 */
	public static void setExistingPath(Path existingPath) {
		Values.existingPath = existingPath;
	}

	/**
	 * @return the inputPath
	 */
	public static Path getInputPath() {
		return inputPath;
	}

	/**
	 * @param inputPath the inputPath to set
	 */
	public static void setInputPath(Path inputPath) {
		Values.inputPath = inputPath;
	}

	/**
	 * @return the outputPath
	 */
	public static Path getOutputPath() {
		return outputPath;
	}

	/**
	 * @return the startTimeOperation
	 */
	public static long getStartTimeOperation() {
		return startTimeOperation;
	}

	/**
	 * @param startTimeOperation the startTimeOperation to set
	 */
	public static void setStartTimeOperation(long startTimeOperation) {
		Values.startTimeOperation = startTimeOperation;
	}

	/**
	 * @return the endTimeOperation
	 */
	public static long getEndTimeOperation() {
		return endTimeOperation;
	}

	/**
	 * @param endTimeOperation the endTimeOperation to set
	 */
	public static void setEndTimeOperation(long endTimeOperation) {
		Values.endTimeOperation = endTimeOperation;
	}

	/**
	 * @return the isDebugMode
	 */
	public static boolean isDebugMode() {
		return isDebugMode;
	}

	/**
	 * @return the jsonFilenamePattern
	 */
	public static String getJsonFilenamePattern() {
		return JSON_FILENAME_PATTERN;
	}

	/**
	 * @return the gson
	 */
	public static Gson getGson() {
		return GSON;
	}
	
	/**
	 * @return the props
	 */
	public static Properties getProps() {
		return PROPS;
	}

	/**
	 * @return the mainClassName
	 */
	public static String getOutterClassName() {
		return OUTTER_CLASS_NAME;
	}

	/**
	 * @return the exceptionTxt
	 */
	public static String getExceptionTxt() {
		return EXCEPTION_TXT;
	}
}
