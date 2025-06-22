package com.dsl.classgen.io;

import java.util.concurrent.ExecutionException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.utils.Utils;

public class Compiler {
    public static void compile() {
        if (!Values.isExistsCompiledPJavaClass()) {
            try {
                Utils.getExecutor().submit(() -> {
                    System.out.println("\nCompiling classes from annotations and generated classes...");
                    String libs = ProcessAnnotation.class.getResource("/libs").getPath();
                    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
                    
                    int opStats = jc.run(null, null, null, 
                    		"-d", Values.getCompilationPath().toString(), 
                    		"--module-path", libs, 
                    		"-sourcepath", "/src/main/java/:" + String.valueOf(Values.getOutputPackagePath()), 
                    		Values.getExistingPJavaGeneratedSourcePath().toString());
                    System.out.println(opStats == 0 ? "\nCompilation was successful!" : "\nAn error occurred while compiling");
                }).get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
                	Thread.currentThread().interrupt();
                };
            }
        }
    }
}

