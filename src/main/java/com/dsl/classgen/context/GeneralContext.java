package com.dsl.classgen.context;

import java.io.IOException;

/**
 * The Class GeneralContext.
 */
public class GeneralContext {

	/** The GeneralContext singleton instance */
	private static final GeneralContext INSTANCE = new GeneralContext();

	/** The flags context instance. */
	private final FlagsContext flagsContextInstance = new FlagsContext();

	/** The paths context instance. */
	private final PathsContext pathsContextInstance = new PathsContext(flagsContextInstance.getIsDebugMode());

	/** The time operation. */
	private long timeOperation;

	/**
	 * Instantiates a new general context.
	 */
	private GeneralContext() {}

	/**
	 * Gets the single instance of GeneralContext.
	 *
	 * @return single instance of GeneralContext
	 */
	public static GeneralContext getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the flags context instance.
	 *
	 * @return the flags context instance
	 */
	public FlagsContext getFlagsContextInstance() {
		return flagsContextInstance;
	}

	/**
	 * Gets the paths context instance.
	 *
	 * @return the paths context instance
	 */
	public PathsContext getPathsContextInstance() {
		return pathsContextInstance;
	}

	/**
	 * Gets the time operation.
	 *
	 * @return the time operation
	 */
	public long getTimeOperation() {
		return timeOperation;
	}

	/**
	 * Sets the time operation.
	 *
	 * @param timeOperation the new time operation
	 */
	public void setTimeOperation(long timeOperation) {
		this.timeOperation = timeOperation;
	}

	/**
	 * Throw IO exception.
	 *
	 * @return the IO exception
	 */
	public static IOException throwIOException() {
		return new IOException("""
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
				""");
	}
}
