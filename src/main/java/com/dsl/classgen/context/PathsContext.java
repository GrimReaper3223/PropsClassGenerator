package com.dsl.classgen.context;

import static com.dsl.classgen.context.FrameworkContext.INSTANCE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.utils.Utils;

public class PathsContext {

	private static final Logger LOGGER = LogManager.getLogger(PathsContext.class);
	
	private final Deque<Map.Entry<Path, ?>> changedFiles;	// armazena eventos de alteracoes em arquivos emitidos pela implementacao do servico de monitoramento de diretorios
	private final List<Path> fileList;						// caso um diretorio inteiro seja processado, os arquivos ficarao aqui
	private final List<Path> dirList;						// caso um ou mais diretorios sejam processados, os diretorios ficarao aqui. O sistema de monitoramento de diretorios se encarrega de processar esta lista
	
	private final String outterClassName;					// nome final da classe externa
	
	// caminhos no sistema de arquivos
	private final Path userDir;
	private final Path cacheDir;
    private Path inputPropertiesPath;					// caminho referente ao arquivo de propriedades (ou diretorio contendo os arquivos de propriedades a serem examinados)
    private Path existingPJavaGeneratedSourcePath;		// caminho referente ao arquivo fonte P.java caso ele exista
    private Path outputSourceDirPath;					// caminho indicando onde o pacote ...generated deve ser criado
    private Path outputSourceFilePath;					// caminho indicando onde o arquivo P.java deve ser escrito, resolvido com o caminho de onde o pacote deve ser criado
    private Path outputClassFilePath;					// caminho indicando onde o arquivo P.class deve ser encontrado, resolvido com o caminho do pacote existente
    
    // informacoes para geracao e formatacao
 	private String propertiesDataType;					// o tipo de dados encontrado no arquivo de propriedades correspondente ao padrao # $javatype:@<tipo_de_dado_java>
    private Path propertiesFileName;					// nome do arquivo de propriedades com a extensao .properties
    private String packageClass;						// pacote que deve ser inserido no cabecalho do arquivo de classe para indicar sua localizacao
    private String generatedClass;						// contem o conteudo da classe gerada. Esta variavel deve ser usada pelo escritor para armazenar os dados no caminho de saida, ou a saida padrao para imprimir na tela, caso o debug esteja habilitado
	
	PathsContext(boolean isDebugMode) {
		
		changedFiles = new ArrayDeque<>();
		fileList = new ArrayList<>();
		dirList = new ArrayList<>();
		
		outterClassName = "P";
		userDir = Paths.get(System.getProperty("user.dir"));
		outputSourceDirPath = userDir.resolve(isDebugMode ? "src/test/java" : "src/main/java");
		outputClassFilePath = Path.of("target").resolve(isDebugMode ? "test/java" : "classes");
		cacheDir = userDir.resolve(".jsonProperties-cache");
	}

	public void resolvePaths(String packageClass) {
        outputSourceDirPath = outputSourceDirPath.resolve(Utils.normalizePath(packageClass, ".", "/"));
        outputSourceFilePath = outputSourceDirPath.resolve(outterClassName + ".java");
    }
	
	// fileList
	public List<Path> getFileList() {
        return fileList;
    }
	
	// dirList
	public List<Path> getDirList() {
        return dirList;
    }
	
	// changedFilesDeque
	public Deque<Map.Entry<Path, ?>> getChangedFilesQueue() {
		return changedFiles;
	}
	
	public void addFileToList(Path filePath) {
		var flagsInstance = FrameworkContext.INSTANCE.flagsContextInstance;
		if(flagsInstance.getIsDirStructureAlreadyGenerated() && flagsInstance.getIsExistsPJavaSource()) {
			if(!CacheManager.hasValidCacheFile(filePath)) {
				CacheManager.addNewCacheFileToWrite(filePath);
			}
		} else {
			CacheManager.addNewCacheFileToWrite(filePath);
		}
    	fileList.add(filePath);
        LOGGER.log(Level.INFO, "Properties file added to file list: {}\n", filePath);
    }
	
    public void addDirToList(Path dirPath) {
    	dirList.add(dirPath);
        LOGGER.log(Level.INFO, "Directory added to dir list: {}\n", dirPath);
    }
	
    // changedFiles
    public <T extends WatchEvent.Kind<?>> void addChangedEntryToQueue(Map.Entry<Path, T> entry) {
    	boolean isSuccessOffering = changedFiles.offer(entry);
   		INSTANCE.flagsContextInstance.setHasChangedFilesLeft(isSuccessOffering || !changedFiles.isEmpty());
    }
    
    public List<Map.Entry<Path, ?>> getAllChangedEntriesFromQueue() {
    	List<Map.Entry<Path, ?>> entryList = new ArrayList<>();
    	
    	do {
    		entryList.add(changedFiles.poll());
    	}
    	while(!changedFiles.isEmpty());
    	INSTANCE.flagsContextInstance.setHasChangedFilesLeft(!changedFiles.isEmpty());
    	
    	return entryList;
    }
    
	/**
	 * @return the inputPropertiesPath
	 */
	public Path getInputPropertiesPath() {
		return inputPropertiesPath;
	}

	/**
	 * @param inputPropertiesPath the inputPropertiesPath to set
	 */
	public void setInputPropertiesPath(Path inputPropertiesPath) {
		this.inputPropertiesPath = inputPropertiesPath;
	}

	/**
	 * @return the existingPJavaGeneratedSourcePath
	 */
	public Path getExistingPJavaGeneratedSourcePath() {
		return existingPJavaGeneratedSourcePath;
	}

	/**
	 * @param existingPJavaGeneratedSourcePath the existingPJavaGeneratedSourcePath to set
	 */
	public void setExistingPJavaGeneratedSourcePath(Path existingPJavaGeneratedSourcePath) {
		this.existingPJavaGeneratedSourcePath = existingPJavaGeneratedSourcePath;
	}

	/**
	 * @return the outputSourceDirPath
	 */
	public Path getOutputSourceDirPath() {
		return outputSourceDirPath;
	}

	/**
	 * @param outputSourceDirPath the outputSourceDirPath to set
	 */
	public void setOutputSourceDirPath(Path outputSourceDirPath) {
		this.outputSourceDirPath = outputSourceDirPath;
	}

	/**
	 * @return the outputSourceFilePath
	 */
	public Path getOutputSourceFilePath() {
		return outputSourceFilePath;
	}

	/**
	 * @param outputSourceFilePath the outputSourceFilePath to set
	 */
	public void setOutputSourceFilePath(Path outputSourceFilePath) {
		this.outputSourceFilePath = outputSourceFilePath;
	}

	/**
	 * @return the outputClassFilePath
	 */
	public Path getOutputClassFilePath() {
		return outputClassFilePath;
	}

	/**
	 * @param outputClassFilePath the outputClassFilePath to set
	 */
	public void setOutputClassFilePath(Path outputClassFilePath) {
		this.outputClassFilePath = outputClassFilePath;
	}

	/**
	 * @return the outterClassName
	 */
	public String getOutterClassName() {
		return outterClassName;
	}

	/**
	 * @return the userDir
	 */
	public Path getUserDir() {
		return userDir;
	}
	
	/**
	 * @return the cacheDir
	 */
	public Path getCacheDir() {
        return cacheDir;
    }
	
	/**
	 * @return the propertiesDataType
	 */
	public String getPropertiesDataType() {
		return propertiesDataType;
	}

	/**
	 * @param propertiesDataType the propertiesDataType to set
	 */
	public void setPropertiesDataType(String propertiesDataType) {
		this.propertiesDataType = propertiesDataType;
	}

	/**
	 * @return the propertiesFileName
	 */
	public Path getPropertiesFileName() {
		return propertiesFileName;
	}
	
	/**
	 * @param propertiesFileName the propertiesFileName to set
	 */
	public void setPropertiesFileName(Path propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	/**
	 * @return the packageClass
	 */
	public String getPackageClass() {
		return packageClass;
	}

	/**
	 * @param packageClass the packageClass to set
	 */
	public void setPackageClass(String packageClass) {
		this.packageClass = packageClass;
	}

	/**
	 * @return the generatedClass
	 */
	public String getGeneratedClass() {
		return generatedClass;
	}

	/**
	 * @param generatedClass the generatedClass to set
	 */
	public void setGeneratedClass(String generatedClass) {
		this.generatedClass = generatedClass;
	}
	
	public Path getSoftPropertiesFileName() {
		String stringPropertiesFileName = propertiesFileName.toString();
		return Path.of(stringPropertiesFileName.substring(0, stringPropertiesFileName.indexOf('.')));
	}
	
	public String getFullPackageClass() {
		return packageClass + "." + outterClassName + ".java";
	}
}
