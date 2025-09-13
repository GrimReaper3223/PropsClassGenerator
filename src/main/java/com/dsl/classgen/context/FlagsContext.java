package com.dsl.classgen.context;

import com.dsl.classgen.core.InspectHotspot;

public class FlagsContext {

	// deve ser true ao depurar o projeto.
	// se true for definido, a classe gerada sera impressa ao inves de escrita
    private boolean isDebugMode = Boolean.parseBoolean(System.getProperty("runtime.debug_mode", "false"));

	// flags
    private boolean isRecursive;						// indica se a recursividade nos diretorios deve ser aplicada
    private boolean hasDirStructureAlreadyGenerated;		// indica se a estrutura que o framework gera ja existe no /src/main/java/*
    private boolean hasExistsPJavaSource;				// indica se o arquivo P.java existe dentro da estrutura existente (se houver uma)
    private boolean isExistsCompiledPJavaClass;			// indica se ja existe uma compilacao do arquivo P.java
    private boolean hasChangedFilesLeft;				// indica se ainda existem eventos gerados pela implementacao do WatchService na fila. Esta variavel deve ser usada como interruptor pelo processador alteracoes em arquivos.
    private boolean isItAlreadyRunning;					// indica se a execucao deste hotspot e unico e nao existe outro em memoria. Isso previne que o framework seja executado duas vezes com os mesmos dados, trazendo inconsistencias e pesando a memoria de execucao. Esse controle e necessario tambem por conveniencia, para que o dev nao precise comentar o codigo de inicializacao do framework

	FlagsContext() {
		/*
		 * Identifica se o framework ja esta em execucao
		 * Essa verificacao e feita buscando o numero de execucoes do hotspot deste modulo
		 * O ideal e que exista apenas uma execucao deste modulo em memoria
		 *
		 * 1 - uma execucao (false, indica que existe uma execucao [a execucao atual]. Permite a execucao desta instancia do framework)
		 * 2 ou + - multiplas execucoes (true, indica que existem multiplas execucoes [a execucao atual e outras], o que impede a execucao do framework)
		 */
		this.isItAlreadyRunning = new InspectHotspot().lookupForHotspotModuleExecution() > 1;
	}

	public boolean isItAlreadyRunning() {
		return isItAlreadyRunning;
	}

	/**
	 * @return the isDebugMode
	 */
	public boolean isDebugMode() {
		return isDebugMode;
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
	public boolean hasDirStructureAlreadyGenerated() {
		return hasDirStructureAlreadyGenerated;
	}

	/**
	 * @param hasDirStructureAlreadyGenerated the isDirStructureAlreadyGenerated to set
	 */
	public void setHasDirStructureAlreadyGenerated(boolean hasDirStructureAlreadyGenerated) {
		this.hasDirStructureAlreadyGenerated = hasDirStructureAlreadyGenerated;
	}

	public boolean hasExistsPJavaSource() {
		return hasExistsPJavaSource;
	}

	/**
	 * @param hasExistsPJavaSource the isExistsPJavaSource to set
	 */
	public void setHasExistsPJavaSource(boolean hasExistsPJavaSource) {
		this.hasExistsPJavaSource = hasExistsPJavaSource;
	}

	public boolean hasSourceStructureGenerated(boolean orExistsInsteadOfAnd) {
		return orExistsInsteadOfAnd ? hasDirStructureAlreadyGenerated || hasExistsPJavaSource : hasDirStructureAlreadyGenerated && hasExistsPJavaSource;
	}

	/**
	 * @return the isExistsCompiledPJavaClass
	 */
	public boolean isExistsCompiledPJavaClass() {
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
	public boolean hasChangedFilesLeft() {
		return hasChangedFilesLeft;
	}

	/**
	 * @param hasChangedFilesLeft the hasChangedFilesLeft to set
	 */
	public void setHasChangedFilesLeft(boolean hasChangedFilesLeft) {
		this.hasChangedFilesLeft = hasChangedFilesLeft;
	}
}
