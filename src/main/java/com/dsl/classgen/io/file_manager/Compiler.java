package com.dsl.classgen.io.file_manager;

import java.util.concurrent.ExecutionException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.Utils;

public final class Compiler extends SupportProvider {
	
	private Compiler() {}
	
    public static void compile() {
    	// a compilacao ocorre caso nao exista a classe P.java compilada. Do contrario, o metodo apenas retorna
        if (!flagsCtx.getIsExistsCompiledPJavaClass()) {
            try {
                Utils.getExecutor().submit(() -> {
                	LOGGER.info("Compiling classes from annotations and generated classes...\n");
                    String libs = AnnotationProcessor.class.getResource("/libs").getPath();
                    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
                    
                    int opStats = jc.run(null, null, null, 
                    		"-d", pathsCtx.getOutputClassFilePath().toString(), 
                    		"--module-path", libs, 
                    		"-sourcepath", "/src/main/java/:" + pathsCtx.getOutputSourceDirPath().toString(), 
                    		pathsCtx.getExistingPJavaGeneratedSourcePath().toString());
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

