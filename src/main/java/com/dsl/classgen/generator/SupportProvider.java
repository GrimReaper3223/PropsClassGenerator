package com.dsl.classgen.generator;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;

public abstract sealed class SupportProvider permits NewInnerStaticClassGenerator, NewInnerFieldGenerator, NewOutterClassGenerator {

	protected static final Logger LOGGER = LogManager.getLogger(SupportProvider.class);
	
	protected final GeneralContext generalCtx = GeneralContext.getInstance();
	protected final PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	protected final FlagsContext flagsCtx = generalCtx.getFlagsContextInstance();
	
	protected SupportProvider() {}
	
    protected void formatGenerationOutput(String input1, String input2, String input3) {
    	LOGGER.info("Generating {} '{}' {}", input1, input2, Objects.isNull(input3) ? "" : input3.equals(System.lineSeparator()) ? "\n" : "[" + input3 + "]");
    }
}
