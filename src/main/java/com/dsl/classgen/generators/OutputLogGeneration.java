package com.dsl.classgen.generators;

import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;

sealed interface OutputLogGeneration permits InnerFieldGenerator, InnerStaticClassGenerator, OutterClassGenerator {
	
	final Logger logger = LogManager.getLogger(OutputLogGeneration.class);
	
	final FrameworkContext fwCtx = FrameworkContext.get();
	final PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	final FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	
    public default void formatGenerationOutput(String input1, String input2, String input3) {
    	logger.log(Level.INFO, "Generating {} '{}' {}", input1, input2, Objects.isNull(input3) ? "" : input3.equals(System.lineSeparator()) ? "\n" : "[" + input3 + "]");
    }
}

