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
	
	// deve ser true ao depurar o projeto. 
	// se true for definido, a classe gerada sera impressa ao inves de escrita
    private static boolean isDebugMode = false;
    
    private static final Properties PROPS = new Properties();		// objeto que vai armazenar as propriedades dos arquivos carregados
    private static final Gson GSON = new Gson();					// objeto gson para escrita do cache
    private static final String OUTTER_CLASS_NAME;					// nome final da classe externa
    
    // estruturas de dados
    private static List<Path> fileList = new ArrayList<Path>();		// caso um diretorio inteiro seja processado, os arquivos ficarao aqui
    private static List<Path> dirList = new ArrayList<Path>();		// caso um ou mais diretorios sejam processados, os diretorios ficarao aqui. O sistema de monitoramento de diretorios se encarrega de processar esta lista 
    private static Deque<Map.Entry<Path, ?>> changedFile = new ArrayDeque<>(128);	// armazena eventos de alteracoes em arquivos emitidos pela implementacao do servico de monitoramento de diretorios
    private static Map<Path, HashTableModel> hashTableModelMap = new HashMap<>();	// mapa cuja chave e o caminho para o arquivo de cache criado/lido. O valor e um objeto que encapsula todos os dados contidos no cache
    
    // flags
    private static boolean isSingleFile;						// indica se o caminho passado corresponde a um unico arquivo 
    private static boolean isRecursive;							// indica se a recursividade nos diretorios deve ser aplicada
    private static boolean isDirStructureAlreadyGenerated;		// indica se a estrutura que o framework gera ja existe no /src/main/java/*
    private static boolean isExistsPJavaSource;					// indica se o arquivo P.java existe dentro da estrutura existente (se houver uma)
    private static boolean isExistsCompiledPJavaClass;			// indica se ja existe uma compilacao do arquivo P.java
    
    // informacoes para geracao e formatacao
    private static String propertiesDataType;					// o tipo de dados encontrado no arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    private static Path rawPropertiesfileName;					// nome do arquivo de propriedades com a extensao .properties
    private static String softPropertiesfileName;				// nome do arquivo de propriedades sem a extensao .properties
    private static String packageClass;							// pacote que deve ser inserido no cabecalho do arquivo de classe para indicar sua localizacao
    private static String packageClassWithOutterClassName;		// o nome do pacote fornecido para a variavel acima + o nome da classe externa (...generated.P)
    private static String generatedClass;						// contem o conteudo da classe gerada. Esta variavel deve ser usada pelo escritor para armazenar os dados no caminho de saida, ou a saida padrao para imprimir na tela, caso o debug esteja habilitado
    
    // caminhos no sistema de arquivos
    private static Path inputPropertiesPath;					// caminho referente ao arquivo de propriedades (ou diretorio contendo os arquivos de propriedades a serem examinados)
    private static Path existingPJavaGeneratedSourcePath;		// caminho referente ao arquivo fonte P.java caso ele exista
    private static Path outputPackagePath;						// caminho indicando onde o pacote ...generated deve ser criado
    private static Path outputFilePath;							// caminho indicando onde o arquivo P.java deve ser escrito resolvido com o caminho de onde o pacote deve ser criado
    private static Path compilationPath;						// caminho indicando onde se encontram os arquivos .class desejados pelo framework
    
    private static final Path CACHE_DIRS;						// caminho referente ao diretorio de cache
    private static final String JSON_FILENAME_PATTERN;			// padrao de nomenclatura para escrita do arquivo json
    
    private static long startTimeOperation;						// guarda o tempo do sistema no momento em que a geracao se inicia
    private static long endTimeOperation;						// guarda o tempo do sistema no mometno em que a geracao finaliza, oferencendo informacoes sobre quanto tempo durou a operacao
    private static final String EXCEPTION_TXT;					// contem o texto de mensagem de excecao caso o padrao de reconhecimento de tipo java nao esteja presente no arquivo de propriedade

    static {
    	OUTTER_CLASS_NAME = "P";
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
        packageClassWithOutterClassName = packageClass + "." + OUTTER_CLASS_NAME;
        outputPackagePath = outputPackagePath.resolve(Utils.normalizePath(packageClass));
        outputFilePath = outputPackagePath.resolve(OUTTER_CLASS_NAME + ".java");
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

