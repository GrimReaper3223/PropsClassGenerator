package com.dsl.classgen.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class Values {
	
    private static boolean isDebugMode = false;
    
    private static final Properties PROPS = new Properties();
    private static final Gson GSON = new Gson();
    private static final String OUTTER_CLASS_NAME = "P";
    
    private static List<Path> fileList = new ArrayList<Path>();
    private static List<Path> dirList = new ArrayList<Path>();
    private static Deque<Map.Entry<Path, ?>> changedFile = new ArrayDeque<>(128);
    private static Map<Path, HashTableModel> hashTableModelMap = new HashMap<Path, HashTableModel>();
    
    private static boolean isSingleFile;
    private static boolean isRecursive;
    private static boolean isDirStructureAlreadyGenerated;
    private static boolean isExistsPJavaSource;
    private static boolean isExistsCompiledPJavaClass;
    
    private static String propertiesDataType;
    private static Path rawPropertiesfileName;
    private static String softPropertiesfileName;
    private static String packageClass;
    private static String packageClassWithOutterClassName;
    private static String generatedClass;
    
    private static Path inputPropertiesPath;
    private static Path existingPJavaGeneratedSourcePath;
    private static Path outputPackagePath;
    private static Path outputFilePath;
    private static Path compilationPath;
    
    private static final Path CACHE_DIRS;
    private static final String JSON_FILENAME_PATTERN;
    
    private static long startTimeOperation;
    private static long endTimeOperation;
    private static final String EXCEPTION_TXT;

    static {
        isExistsPJavaSource = false;
        outputPackagePath = Path.of("src", "main", "java");
        compilationPath = Paths.get(System.getProperty("user.dir"), "target", "classes");
        CACHE_DIRS = Path.of(System.getProperty("user.dir"), ".jsonProperties-cache");
        JSON_FILENAME_PATTERN = "%s-cache.json";
        startTimeOperation = 0L;
        endTimeOperation = 0L;
        EXCEPTION_TXT = """
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
    }
    
    public static void resolvePaths() {
        packageClassWithOutterClassName = packageClass + ".P";
        outputPackagePath = outputPackagePath.resolve(Utils.normalizePath(packageClass));
        outputFilePath = outputPackagePath.resolve("P.java");
    }

    public static <T extends WatchEvent.Kind<?>> void addChangedValueToMap(Map.Entry<Path, T> entry) {
        changedFile.offer(entry);
    }

    public static List<Path> getDirList() {
        return dirList;
    }

    public static void addDirToList(Path dirPath) {
        dirList.add(dirPath);
    }

    public static List<Path> getFileList() {
        return fileList;
    }

    public static void addFileToList(Path filePath) {
        fileList.add(filePath);
    }

    public static void setFileList(List<Path> fileList) {
        Values.fileList = fileList;
    }

    public static boolean isDirStructureAlreadyGenerated() {
        return isDirStructureAlreadyGenerated;
    }

    public static void setIfDirStructureAlreadyGenerated(boolean dirStructureAlreadyGenerated) {
        isDirStructureAlreadyGenerated = dirStructureAlreadyGenerated;
    }

    public static HashTableModel getElementFromHashTableMap(Path key) {
        return hashTableModelMap.get(key);
    }

    public static void putElementIntoHashTableMap(Path key, HashTableModel value) {
        hashTableModelMap.put(key, value);
    }

    public static void cleanHashTableMap() {
        hashTableModelMap.clear();
    }

    public static boolean isSingleFile() {
        return isSingleFile;
    }

    public static void setIsSingleFile(boolean isSingleFile) {
        Values.isSingleFile = isSingleFile;
    }

    public static boolean isRecursive() {
        return isRecursive;
    }

    public static void setIsRecursive(boolean isRecursive) {
        Values.isRecursive = isRecursive;
    }

    public static String getPropertiesDataType() {
        return propertiesDataType;
    }

    public static void setPropertiesDataType(String propertiesDataType) {
        Values.propertiesDataType = propertiesDataType;
    }

    public static Path getRawPropertiesfileName() {
        return rawPropertiesfileName;
    }

    public static void setRawPropertiesfileName(Path rawPropertiesfileName) {
        Values.rawPropertiesfileName = rawPropertiesfileName;
    }

    public static String getSoftPropertiesFileName() {
        return softPropertiesfileName;
    }

    public static void setSoftPropertiesfileName(String softPropertiesfileName) {
        Values.softPropertiesfileName = softPropertiesfileName;
    }

    public static Path getCacheDirs() {
        return CACHE_DIRS;
    }

    public static String getPackageClassWithOutterClassName() {
        return packageClassWithOutterClassName;
    }

    public static String getPackageClass() {
        return packageClass;
    }

    public static void setPackageClass(String packageClass) {
        Values.packageClass = packageClass;
    }

    public static String getGeneratedClass() {
        return generatedClass;
    }

    public static void setGeneratedClass(String generatedClass) {
        Values.generatedClass = generatedClass;
    }

    public static Path getExistingPJavaGeneratedSourcePath() {
        return existingPJavaGeneratedSourcePath;
    }

    public static void setExistingPJavaGeneratedSourcePath(Path existingPJavaGeneratedSourcePath) {
        Values.existingPJavaGeneratedSourcePath = existingPJavaGeneratedSourcePath;
        isExistsPJavaSource = true;
    }

    public static boolean isExistsPJavaSource() {
        return isExistsPJavaSource;
    }

    public static boolean isExistsCompiledPJavaClass() {
        return isExistsCompiledPJavaClass;
    }

    public static void setIfExistsCompiledPJavaClass(boolean isExistsCompiledPJavaClass) {
        Values.isExistsCompiledPJavaClass = isExistsCompiledPJavaClass;
    }

    public static Path getInputPropertiesPath() {
        return inputPropertiesPath;
    }

    public static void setInputPropertiesPath(Path inputPropertiesPath) {
        Values.inputPropertiesPath = inputPropertiesPath;
    }

    public static Path getOutputPackagePath() {
        return outputPackagePath;
    }

    public static Path getOutputFilePath() {
        return outputFilePath;
    }

    public static Path getCompilationPath() {
        return compilationPath;
    }

    public static void setCompilationPath(Path compilationPath) {
        Values.compilationPath = compilationPath;
    }

    public static long getStartTimeOperation() {
        return startTimeOperation;
    }

    public static void setStartTimeOperation(long startTimeOperation) {
        Values.startTimeOperation = startTimeOperation;
    }

    public static long getEndTimeOperation() {
        return endTimeOperation;
    }

    public static void setEndTimeOperation(long endTimeOperation) {
        Values.endTimeOperation = endTimeOperation;
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    public static String getJsonFilenamePattern() {
        return JSON_FILENAME_PATTERN;
    }

    public static Gson getGson() {
        return GSON;
    }

    public static Properties getProps() {
        return PROPS;
    }

    public static String getOutterClassName() {
        return OUTTER_CLASS_NAME;
    }

    public static String getExceptionTxt() {
        return EXCEPTION_TXT;
    }
}

