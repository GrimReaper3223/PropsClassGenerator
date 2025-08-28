package com.dsl.classgen.context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.service.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public class PathsContext {

	private static final Logger LOGGER = LogManager.getLogger(PathsContext.class);
	private static final Level SUCCESS = Level.getLevel("SUCCESS");
	public Lock locker = new ReentrantLock();

	private final ConcurrentMap<Kind<Path>, Set<Path>> changedFiles;	// armazena eventos de alteracoes em arquivos emitidos pela implementacao do servico de monitoramento de diretorios
	private final Set<Path> fileList;														// caso um diretorio inteiro seja processado, os arquivos ficarao aqui
	private final Set<Path> dirList;														// caso um ou mais diretorios sejam processados, os diretorios ficarao aqui. O sistema de monitoramento de diretorios se encarrega de processar esta lista

	private final String outterClassName;													// nome final da classe externa

	// caminhos no sistema de arquivos
	private final Path userDir;
	private final Path cacheDir;
    private Path inputPath;								// caminho referente ao arquivo de propriedades (ou diretorio contendo os arquivos de propriedades a serem examinados)
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
		changedFiles = new ConcurrentHashMap<>();
		fileList = new HashSet<>();
		dirList = new HashSet<>();

		outterClassName = "P";
		userDir = Paths.get(System.getProperty("user.dir"));
		outputSourceDirPath = userDir.resolve(isDebugMode ? "src/test/java" : "src/main/java");
		outputClassFilePath = Path.of("target").resolve(isDebugMode ? "test-classes" : "classes");
		cacheDir = userDir.resolve(".jsonProperties-cache");
	}

	// fileList
	public Set<Path> getFileSet() {
        return fileList;
    }

	// dirList
	public Set<Path> getDirSet() {
        return dirList;
    }

	public boolean isDirListEmpty() {
		return dirList.isEmpty();
	}

	public void queueFile(Path filePath) {
    	fileList.add(filePath);
    	LOGGER.log(SUCCESS,"File added to file list: {}", filePath);
    }

    public void queueDir(Path dirPath) {
    	if(!WatchServiceImpl.verifyValue(dirPath)) {
    		dirList.add(dirPath);
    		LOGGER.log(SUCCESS, "Directory added to dir list: {}", dirPath);
    	}
    }

    // changedFiles
    public void queueChangedFileEntry(Map.Entry<Kind<Path>, Path> entry) {
    	changedFiles.computeIfAbsent(entry.getKey(), _ -> new HashSet<>()).add(entry.getValue());
    }

    public Set<Entry<Kind<Path>, Set<Path>>> getMappedChangedFiles() {
    	var createdEvent = StandardWatchEventKinds.ENTRY_CREATE;
		var modifiedEvent = StandardWatchEventKinds.ENTRY_MODIFY;
    	if(changedFiles.containsKey(createdEvent)) {
    		changedFiles.get(createdEvent).removeIf(Files::isDirectory);
    	}
    	if(changedFiles.containsKey(modifiedEvent)) {
    		changedFiles.get(modifiedEvent).removeIf(Files::isDirectory);
    	}
    	return changedFiles.entrySet();
    }

    public boolean isEmptyChangedFilesMap() {
    	return changedFiles.isEmpty();
    }

    public void clearMapOfChanges() {
    	changedFiles.clear();
    }

	/**
	 * @return the inputPropertiesPath
	 */
	public Path getInputPath() {
		return inputPath;
	}

	/**
	 * @param inputPath the inputPropertiesPath to set
	 */
	public void setInputPath(Path inputPath) {
		this.inputPath = inputPath;
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
		var flagsCtx = GeneralContext.getInstance().getFlagsContextInstance();
		if(!flagsCtx.getIsDirStructureAlreadyGenerated()) {
	        this.outputSourceDirPath = outputSourceDirPath.resolve(Utils.normalizePath(this.packageClass, ".", "/"));
	        this.outputSourceFilePath = this.outputSourceDirPath.resolve(outterClassName + ".java");
		} else {
			this.outputSourceDirPath = outputSourceDirPath;
		}
	}

	/**
	 * @return the outputSourceFilePath
	 */
	public Path getOutputSourceFilePath() {
		return outputSourceFilePath;
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
		this.packageClass = Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString();
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

	public String getFullPackageClass() {
		return packageClass + "." + outterClassName + ".java";
	}
}
