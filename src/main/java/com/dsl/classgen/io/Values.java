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
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.io.cache_system.FileAnalyzer;
import com.dsl.classgen.io.cache_system.HashTableModel;
import com.dsl.classgen.utils.Utils;

public final class Values {
	
	private static final Logger LOGGER = LogManager.getLogger(Values.class);
	
	// deve ser true ao depurar o projeto. 
	// se true for definido, a classe gerada sera impressa ao inves de escrita
    private static boolean isDebugMode = false;
    
    private static final Properties PROPS;							// objeto que vai armazenar as propriedades dos arquivos carregados
    private static final String OUTTER_CLASS_NAME;					// nome final da classe externa
    private static final String USER_DIR;
    
    // estruturas de dados
    private static Deque<Path> newCacheToWriteDeque = new ArrayDeque<Path>();	// deve armazenar arquivos para processamento de cache. Quando novos arquivos forem fornecidos para a lista de arquivos ou individualmente, uma entrada correspondente deve ser criada aqui
    private static List<Path> fileList = new ArrayList<Path>();				// caso um diretorio inteiro seja processado, os arquivos ficarao aqui
    private static List<Path> dirList = new ArrayList<Path>();				// caso um ou mais diretorios sejam processados, os diretorios ficarao aqui. O sistema de monitoramento de diretorios se encarrega de processar esta lista 
    private static Deque<Map.Entry<Path, ?>> changedFileDeque = new ArrayDeque<>();		// armazena eventos de alteracoes em arquivos emitidos pela implementacao do servico de monitoramento de diretorios
    private static Map<Path, HashTableModel> hashTableModelMap = new HashMap<>();		// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache
    
    // flags
    private static boolean isSingleFile;						// indica se o caminho passado corresponde a um unico arquivo 
    private static boolean isRecursive;							// indica se a recursividade nos diretorios deve ser aplicada
    private static boolean isDirStructureAlreadyGenerated;		// indica se a estrutura que o framework gera ja existe no /src/main/java/*
    private static boolean isExistsPJavaSource;					// indica se o arquivo P.java existe dentro da estrutura existente (se houver uma)
    private static boolean isExistsCompiledPJavaClass;			// indica se ja existe uma compilacao do arquivo P.java
    private static boolean hasChangesRemaining;					// indica se ainda existem eventos gerados pela implementacao do WatchService na fila. Esta variavel deve ser usada como interruptor pelo processador alteracoes em arquivos.							
    
    // informacoes para geracao e formatacao
    private static String propertiesDataType;					// o tipo de dados encontrado no arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    private static Path rawPropertiesfileName;					// nome do arquivo de propriedades com a extensao .properties
    private static Path softPropertiesfileName;					// nome do arquivo de propriedades sem a extensao .properties
    private static String packageClass;							// pacote que deve ser inserido no cabecalho do arquivo de classe para indicar sua localizacao
    private static String packageClassWithOutterClassName;		// o nome do pacote fornecido para a variavel acima + o nome da classe externa (...generated.P)
    private static String generatedClass;						// contem o conteudo da classe gerada. Esta variavel deve ser usada pelo escritor para armazenar os dados no caminho de saida, ou a saida padrao para imprimir na tela, caso o debug esteja habilitado
    
    // caminhos no sistema de arquivos
    private static Path inputPropertiesPath;					// caminho referente ao arquivo de propriedades (ou diretorio contendo os arquivos de propriedades a serem examinados)
    private static Path existingPJavaGeneratedSourcePath;		// caminho referente ao arquivo fonte P.java caso ele exista
    private static Path outputSourceDirPath;						// caminho indicando onde o pacote ...generated deve ser criado
    private static Path outputSourceFilePath;					// caminho indicando onde o arquivo P.java deve ser escrito, resolvido com o caminho de onde o pacote deve ser criado
    private static Path outputClassFilePath;					// caminho indicando onde o arquivo P.class deve ser encontrado, resolvido com o caminho do pacote existente
    
    private static long startTimeOperation;						// guarda o tempo do sistema no momento em que a geracao se inicia

    static {
    	PROPS = new Properties();
    	
    	USER_DIR = System.getProperty("user.dir");
    	OUTTER_CLASS_NAME = "P";
        isExistsPJavaSource = false;
        outputSourceDirPath = Paths.get(USER_DIR, isDebugMode ? "src/test/java" : "src/main/java");
        startTimeOperation = 0L;
    }
    
    public static void resolvePaths() {
        packageClassWithOutterClassName = packageClass + "." + OUTTER_CLASS_NAME;
        outputSourceDirPath = outputSourceDirPath.resolve(Utils.normalizePath(packageClass, ".", "/"));
        outputSourceFilePath = outputSourceDirPath.resolve(OUTTER_CLASS_NAME + ".java");
    }

    public static <T extends WatchEvent.Kind<?>> void addChangedValueToDeque(Map.Entry<Path, T> entry) {
        changedFileDeque.offer(entry);
        hasChangesRemaining = true;
    }
    
    public static List<Map.Entry<Path, ?>> getAllChangedValuesFromDeque() {
    	List<Map.Entry<Path, ?>> entryList = new ArrayList<>();
    	
    	while(hasChangesRemaining) {
    		entryList.add(changedFileDeque.poll());
    		if(changedFileDeque.isEmpty()) {
    			hasChangesRemaining = false;
    		}
    	}
    	return entryList;
    }
    
    public static List<Path> getAllCacheFilesToWriteFromDeque() {
    	List<Path> cacheList = new ArrayList<>();
    	
    	while(newCacheToWriteDeque.size() > 0) {
    		cacheList.add(newCacheToWriteDeque.poll());
    	}
    	return cacheList;
    }
    
    public static boolean containsCacheToProcess() {
    	return !newCacheToWriteDeque.isEmpty();
    }

    public static List<Path> getDirList() {
        return dirList;
    }

    public static void addDirToList(Path dirPath) {
        dirList.add(dirPath);
        LOGGER.log(Level.INFO, "Directory added to dir list: {}\n", dirPath);
    }

    public static List<Path> getFileList() {
        return fileList;
    }

    public static void addFileToList(Path filePath) {
    	if(!FileAnalyzer.hasValidCacheFile(filePath)) {
    		newCacheToWriteDeque.add(filePath);
    	}
        fileList.add(filePath);
        LOGGER.log(Level.INFO, "Properties file added to file list: {}\n", filePath);
    }

    public static void setFileList(List<Path> fileList) {
    	newCacheToWriteDeque.addAll(fileList);
        Values.fileList = fileList;
    }

    public static boolean checkRemainingChanges() {
    	return hasChangesRemaining;
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

    public static void computeElementIntoHashTableMap(Path key, HashTableModel value) {
        hashTableModelMap.computeIfPresent(key, (_, _) -> value);
    }

    public static void deleteElementFromHashTableMap(Path key) {
		hashTableModelMap.remove(key);
    }
    
    public static int getHashTableMapSize() {
    	return hashTableModelMap.size();
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

    public static Path getSoftPropertiesFileName() {
        return softPropertiesfileName;
    }

    public static void setSoftPropertiesfileName(Path softPropertiesfileName) {
        Values.softPropertiesfileName = softPropertiesfileName;
    }

    public static Path getCacheDir() {
        return Paths.get(USER_DIR, ".jsonProperties-cache");
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

    public static Path getInputPropertiesPath() {
        return inputPropertiesPath;
    }

    public static void setInputPropertiesPath(Path inputPropertiesPath) {
        Values.inputPropertiesPath = inputPropertiesPath;
    }

    public static Path getOutputSourceDirPath() {
        return outputSourceDirPath;
    }

    public static Path getOutputSourceFilePath() {
        return outputSourceFilePath;
    }
    
    public static Path getOutputClassFilePath() {
    	return outputClassFilePath;
    }
    
    public static void setOutputClassFilePath(Path outputClassFilePath) {
    	Values.outputClassFilePath = outputClassFilePath;
    	isExistsCompiledPJavaClass = Objects.nonNull(outputClassFilePath);
    }

    public static Path getCompilationPath() {
        return Paths.get(USER_DIR, "target", "classes");
    }

    public static long getStartTimeOperation() {
        return startTimeOperation;
    }

    public static void setStartTimeOperation(long startTimeOperation) {
        Values.startTimeOperation = startTimeOperation;
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    public static Properties getProps() {
        return PROPS;
    }

    public static String getOutterClassName() {
        return OUTTER_CLASS_NAME;
    }

    public static String getExceptionTxt() {
        return """
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
}

