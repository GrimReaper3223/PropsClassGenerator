package com.dsl.classgen.context;

public class FlagsContext {

	// deve ser true ao depurar o projeto.
	// se true for definido, a classe gerada sera impressa ao inves de escrita
    private boolean isDebugMode = Boolean.parseBoolean(System.getProperty("runtime.debug_mode", "false"));

	// flags
    private boolean isSingleFile;						// indica se o caminho passado corresponde a um unico arquivo
    private boolean isRecursive;						// indica se a recursividade nos diretorios deve ser aplicada
    private boolean isDirStructureAlreadyGenerated;		// indica se a estrutura que o framework gera ja existe no /src/main/java/*
    private boolean isExistsPJavaSource;				// indica se o arquivo P.java existe dentro da estrutura existente (se houver uma)
    private boolean isExistsCompiledPJavaClass;			// indica se ja existe uma compilacao do arquivo P.java
    private boolean hasChangedFilesLeft;				// indica se ainda existem eventos gerados pela implementacao do WatchService na fila. Esta variavel deve ser usada como interruptor pelo processador alteracoes em arquivos.

	FlagsContext() {}

	/**
	 * @return the isDebugMode
	 */
	public  boolean getIsDebugMode() {
		return isDebugMode;
	}

	/**
	 * @return the isSingleFile
	 */
	public boolean getIsSingleFile() {
		return isSingleFile;
	}

	/**
	 * @param isSingleFile the isSingleFile to set
	 */
	public void setIsSingleFile(boolean isSingleFile) {
		this.isSingleFile = isSingleFile;
	}

	/**
	 * @return the isRecursive
	 */
	public boolean getRecursiveOption() {
		return isRecursive;
	}

	/**
	 * @param isRecursive the isRecursive to set
	 */
	public void setRecursion(boolean isRecursive) {
		this.isRecursive = isRecursive;
	}

	/**
	 * @return the isDirStructureAlreadyGenerated
	 */
	public boolean getIsDirStructureAlreadyGenerated() {
		return isDirStructureAlreadyGenerated;
	}

	/**
	 * @param isDirStructureAlreadyGenerated the isDirStructureAlreadyGenerated to set
	 */
	public void setIsDirStructureAlreadyGenerated(boolean isDirStructureAlreadyGenerated) {
		this.isDirStructureAlreadyGenerated = isDirStructureAlreadyGenerated;
	}

	/**
	 * @return the isExistsPJavaSource
	 */
	public boolean getIsExistsPJavaSource() {
		return isExistsPJavaSource;
	}

	/**
	 * @param isExistsPJavaSource the isExistsPJavaSource to set
	 */
	public void setIsExistsPJavaSource(boolean isExistsPJavaSource) {
		this.isExistsPJavaSource = isExistsPJavaSource;
	}

	/**
	 * @return the isExistsCompiledPJavaClass
	 */
	public boolean getIsExistsCompiledPJavaClass() {
		return isExistsCompiledPJavaClass;
	}

	/**
	 * @param isExistsCompiledPJavaClass the isExistsCompiledPJavaClass to set
	 */
	public void setIsExistsCompiledPJavaClass(boolean isExistsCompiledPJavaClass) {
		this.isExistsCompiledPJavaClass = isExistsCompiledPJavaClass;
	}

	/**
	 * @return the hasChangedFilesLeft
	 */
	public boolean getHasChangedFilesLeft() {
		return hasChangedFilesLeft;
	}

	/**
	 * @param hasChangedFilesLeft the hasChangedFilesLeft to set
	 */
	public void setHasChangedFilesLeft(boolean hasChangedFilesLeft) {
		this.hasChangedFilesLeft = hasChangedFilesLeft;
	}
}
