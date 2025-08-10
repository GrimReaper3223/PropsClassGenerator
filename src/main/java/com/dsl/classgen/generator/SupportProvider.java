package com.dsl.classgen.generator;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;

/**
 * The Class SupportProvider.
 */
public abstract sealed class SupportProvider
		permits InnerStaticClassGenerator, InnerFieldGenerator, OutterClassGenerator {

	/** The Constant LOGGER. */
	protected static final Logger LOGGER = LogManager.getLogger(SupportProvider.class);

	/** The general ctx. */
	protected final GeneralContext generalCtx = GeneralContext.getInstance();

	/** The paths ctx. */
	protected final PathsContext pathsCtx = generalCtx.getPathsContextInstance();

	/** The flags ctx. */
	protected final FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();

	/**
	 * Instantiates a new support provider.
	 */
	protected SupportProvider() {}

	/**
	 * Format generation output.
	 *
	 * @param input1 the input 1
	 * @param input2 the input 2
	 * @param input3 the input 3
	 */
	protected void formatGenerationOutput(String input1, String input2, String input3) {
		LOGGER.info("Generating {} '{}' {}", input1, input2,
				Objects.isNull(input3) ? "" : input3.equals(System.lineSeparator()) ? "\n" : "[" + input3 + "]");
	}
}
