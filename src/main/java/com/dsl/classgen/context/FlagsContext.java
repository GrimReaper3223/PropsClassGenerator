package com.dsl.classgen.context;

/**
 * The Class FlagsContext.
 */
public class FlagsContext {

	// deve ser true ao depurar o projeto.
	// se true for definido, a classe gerada sera impressa ao inves de escrita
	private boolean isDebugMode = false;

	// flags
	private boolean isSingleFile;
	private boolean isRecursive;
	private boolean isDirStructureAlreadyGenerated;
	private boolean isExistsPJavaSource;
	private boolean isExistsCompiledPJavaClass;
	private boolean hasChangedFilesLeft;

	/**
	 * Instantiates a new flags context.
	 */
	FlagsContext() {}

	/**
	 * Gets the checks if is debug mode.
	 *
	 * @return the isDebugMode option
	 */
	public boolean getIsDebugMode() {
		return isDebugMode;
	}

	/**
	 * Gets the checks if is single file.
	 *
	 * @return the isSingleFile flag
	 */
	public boolean getIsSingleFile() {
		return isSingleFile;
	}

	/**
	 * Sets the checks if is single file.
	 *
	 * @param isSingleFile the isSingleFile to set
	 */
	public void setIsSingleFile(boolean isSingleFile) {
		this.isSingleFile = isSingleFile;
	}

	/**
	 * Gets the checks if is recursive.
	 *
	 * @return the isRecursive option
	 */
	public boolean getIsRecursive() {
		return isRecursive;
	}

	/**
	 * Sets the checks if is recursive.
	 *
	 * @param isRecursive the isRecursive to set
	 */
	public void setIsRecursive(boolean isRecursive) {
		this.isRecursive = isRecursive;
	}

	/**
	 * Gets the checks if is dir structure already generated.
	 *
	 * @return the isDirStructureAlreadyGenerated flag
	 */
	public boolean getIsDirStructureAlreadyGenerated() {
		return isDirStructureAlreadyGenerated;
	}

	/**
	 * Sets the checks if is dir structure already generated.
	 *
	 * @param isDirStructureAlreadyGenerated the isDirStructureAlreadyGenerated to
	 *                                       set
	 */
	public void setIsDirStructureAlreadyGenerated(boolean isDirStructureAlreadyGenerated) {
		this.isDirStructureAlreadyGenerated = isDirStructureAlreadyGenerated;
	}

	/**
	 * Gets the checks if is exists P java source.
	 *
	 * @return the isExistsPJavaSource flag
	 */
	public boolean getIsExistsPJavaSource() {
		return isExistsPJavaSource;
	}

	/**
	 * Sets the checks if is exists P java source.
	 *
	 * @param isExistsPJavaSource the isExistsPJavaSource to set
	 */
	public void setIsExistsPJavaSource(boolean isExistsPJavaSource) {
		this.isExistsPJavaSource = isExistsPJavaSource;
	}

	/**
	 * Gets the checks if is exists compiled P java class.
	 *
	 * @return the isExistsCompiledPJavaClass flag
	 */
	public boolean getIsExistsCompiledPJavaClass() {
		return isExistsCompiledPJavaClass;
	}

	/**
	 * Sets the checks if is exists compiled P java class.
	 *
	 * @param isExistsCompiledPJavaClass the isExistsCompiledPJavaClass to set
	 */
	public void setIsExistsCompiledPJavaClass(boolean isExistsCompiledPJavaClass) {
		this.isExistsCompiledPJavaClass = isExistsCompiledPJavaClass;
	}

	/**
	 * Gets the checks for changed files left.
	 *
	 * @return the hasChangedFilesLeft flag
	 */
	public boolean getHasChangedFilesLeft() {
		return hasChangedFilesLeft;
	}

	/**
	 * Sets the checks for changed files left.
	 *
	 * @param hasChangedFilesLeft the hasChangedFilesLeft to set
	 */
	public void setHasChangedFilesLeft(boolean hasChangedFilesLeft) {
		this.hasChangedFilesLeft = hasChangedFilesLeft;
	}
}
