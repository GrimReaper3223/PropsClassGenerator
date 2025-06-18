package com.dsl.classgen.io;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.gson.Gson;

public final class Values {

	// deve ser true durante o desenvolvimento
	private static boolean isDebugMode = false;
	
	private static final Properties PROPS = new Properties();			// deve armazenar o arquivo de propriedades carregado
	private static final Gson GSON = new Gson();						// instancia para produzie jsons
	private static final String OUTTER_CLASS_NAME = "P";				// nome fixo da classe envolvente das classes aninhadas estaticas
	
	private static List<Path> fileList = new ArrayList<>();				// lista de arquivos se for requisitado geracao multiarquivos
	private static List<Path> dirList = new ArrayList<>();				// lista de diretorios se for requisitada recursao, ou apenas 1 diretorio (diretorio que contem o arquivo unico da propriedade) se somente um unico arquivo for indicado para geracao
	
	private static Deque<Map.Entry<Path, ?>> changedFile = new ArrayDeque<>(128);				// WatchService deve "postar" as alteracoes dos arquivos aqui para que a unidade de processamento possa recolher
	private static Map<String, HashTableModel> hashTableFieldModelMap = new HashMap<>();		// contem a tabela hash de chave-valor dos arquivos de propriedades
	
	private static boolean isSingleFile;								// indica se e um unico arquivo
	private static boolean isRecursive;									// indica se a geracao eve ser recursiva
	private static boolean isDirStructureAlreadyGenerated;				// indica se a estrutura de diretorios ja foi gerada  
	private static boolean isExistsPJavaGeneratedSourcePath;			// indica se o arquivo P.java existe  
	private static boolean isExistsCompiledPJavaClass;					// indica se o arquivo binario esta compilado  
	
	private static String propertiesDataType;							// tipo de dado vigente para geracao de todas as propriedades de um unico arquivo de propriedades.
	
	private static String propertiesfileName;							// nome do arquivo de propriedades sem a extensao
	private static String packageClass;									// pacote dentro de /src/main/java (padrao do sistema) onde deve ser escrito o arquivo de propriedades
	private static String packageClassWithOutterClassName;				// pacote definido acima com o nome da classe principal ao final 
	private static String generatedClass;								// String contendo toda a classe gerada
	private static Path inputPropertiesPath;							// caminho do arquivo de propriedades passado para Generator.init();
	private static Path existingPJavaGeneratedSourcePath;				// caminho do arquivo P.java gerado
	private static Path outputPackagePath = Path.of("src", "main", "java");	// caminho padrao de saida. Deve ser concatenado com o caminho de pacote recebido
	private static Path outputFilePath;									// caminho onde os dados devem ser escritos
	private static Path compilationPath = Path.of("target", "classes");		// caminho para o diretorio de recursos compiladosS
	
	private static final Path CACHE_DIRS = Path.of(System.getProperty("user.dir"), ".jsonProperties-cache");	// diretorio onde o cache de arquivos deve ser montado
	private static final String JSON_FILENAME_PATTERN = "%s-cache.json";	// <nome_do_arquivo>-cache.json
	
	private static long startTimeOperation = 0L;	// armazena o tempo de inicio da operacao de geracao
	private static long endTimeOperation = 0L;		// armazena o tempo de final da operacao de geracao
	
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
		packageClassWithOutterClassName = packageClass + "." + OUTTER_CLASS_NAME;
		outputPackagePath = outputPackagePath.resolve(Path.of(packageClass.replaceAll("[.]", "/")));
		outputFilePath = outputPackagePath.resolve(OUTTER_CLASS_NAME + ".java");
	}
	
	public static <T extends Kind<?>> void addChangedValueToMap(Entry<Path, T> entry) {
		changedFile.offer(entry);
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

	public static boolean isDirStructureAlreadyGenerated() {
		return isDirStructureAlreadyGenerated;
	}

	public static void setIfFileStructureAlreadyGenerated(boolean fileStructureAlreadyGenerated) {
		Values.isDirStructureAlreadyGenerated = fileStructureAlreadyGenerated;
	}
	
	/**
	 * @return the hashTable
	 */
	public static HashTableModel getElementFromHashTableMap(String key) {
		return hashTableFieldModelMap.get(key);
	}

	/**
	 * Inserts new cache elements into the cache map
	 */
	public static void putElementIntoHashTableMap(String key, HashTableModel value) {
		hashTableFieldModelMap.put(key, value);
	}
	
	/*
	 * Clean hash table elements
	 */
	public static void cleanHashTableMap() {
		hashTableFieldModelMap.clear();
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
	 * @return the packageClassWithOutterClassName
	 */
	public static String getPackageClassWithOutterClassName() {
		return packageClassWithOutterClassName;
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
	 * @return the existingPJavaGeneratedSourcePath
	 */
	public static Path getExistingPJavaGeneratedSourcePath() {
		return existingPJavaGeneratedSourcePath;
	}

	/**
	 * @param existingPJavaGeneratedSourcePath the existingPJavaGeneratedSourcePath to set
	 */
	public static void setExistingPJavaGeneratedSourcePath(Path existingPJavaGeneratedSourcePath) {
		Values.existingPJavaGeneratedSourcePath = existingPJavaGeneratedSourcePath;
	}

	/**
	 * @return the isExistsPJavaGeneratedSourcePath
	 */
	public static boolean isExistsPJavaGeneratedSourcePath() {
		return isExistsPJavaGeneratedSourcePath;
	}

	/**
	 * @param isExistsPJavaGeneratedSourcePath the isExistsPJavaGeneratedSourcePath to set
	 */
	public static void setIfExistsPJavaGeneratedSourcePath(boolean isExistsPJavaGeneratedSourcePath) {
		Values.isExistsPJavaGeneratedSourcePath = isExistsPJavaGeneratedSourcePath;
	}

	/**
	 * @return the isExistsCompiledPJavaClass
	 */
	public static boolean isExistsCompiledPJavaClass() {
		return isExistsCompiledPJavaClass;
	}

	/**
	 * @param isExistsCompiledPJavaClass the isExistsCompiledPJavaClass to set
	 */
	public static void setIfExistsCompiledPJavaClass(boolean isExistsCompiledPJavaClass) {
		Values.isExistsCompiledPJavaClass = isExistsCompiledPJavaClass;
	}

	/**
	 * @return the inputPropertiesPath
	 */
	public static Path getInputPropertiesPath() {
		return inputPropertiesPath;
	}

	/**
	 * @param inputPropertiesPath the inputPropertiesPath to set
	 */
	public static void setInputPropertiesPath(Path inputPropertiesPath) {
		Values.inputPropertiesPath = inputPropertiesPath;
	}

	/**
	 * @return the outputPath
	 */
	public static Path getOutputPackagePath() {
		return outputPackagePath;
	}

	/**
	 * @return the outputFilePath
	 */
	public static Path getOutputFilePath() {
		return outputFilePath;
	}

	/**
	 * @return the compilationPath
	 */
	public static Path getCompilationPath() {
		return compilationPath;
	}

	/**
	 * @param compilationPath the compilationPath to set
	 */
	public static void setCompilationPath(Path compilationPath) {
		Values.compilationPath = compilationPath;
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
