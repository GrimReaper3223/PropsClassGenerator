package com.dsl.classgen.io.file_manager;

import java.util.concurrent.ExecutionException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.utils.Utils;

public class Compiler {
	
	private static final Logger LOGGER = LogManager.getLogger(Compiler.class);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	private static PathsContext pathCtx = fwCtx.getPathsContextInstance();
	
	private Compiler() {}
	
    public static void compile() {
    	// a compilacao ocorre caso nao exista a classe P.java compilada. Do contrario, o metodo apenas retorna
        if (!flagsCtx.getIsExistsCompiledPJavaClass()) {
            try {
                Utils.getExecutor().submit(() -> {
                	LOGGER.info("Compiling classes from annotations and generated classes...\n");
                    String libs = ProcessAnnotation.class.getResource("/libs").getPath();
                    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
                    
                    int opStats = jc.run(null, null, null, 
                    		"-d", pathCtx.getOutputClassFilePath().toString(), 
                    		"--module-path", libs, 
                    		"-sourcepath", "/src/main/java/:" + pathCtx.getOutputSourceDirPath().toString(), 
                    		pathCtx.getExistingPJavaGeneratedSourcePath().toString());
                    if(opStats == 0) {
                    	LOGGER.warn("Compilation was successful!\n");
                    } else {
                    	LOGGER.error("An error occurred while compiling\n");
                    }
                }).get();
            }
            catch (InterruptedException | ExecutionException e) {
            	LOGGER.error(e.getMessage(), e);
                if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
                	Thread.currentThread().interrupt();
                }
            }
        }
    }
}

