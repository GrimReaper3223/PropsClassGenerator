package com.dsl.classgen.io;

import java.util.concurrent.ExecutionException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.utils.Utils;

public class Compiler {

	public static void compile() throws ClassNotFoundException, InterruptedException, ExecutionException {
		if(Values.isExistsCompiledPJavaClass()) {
			Utils.getExecutor().submit(() -> {
				System.out.println("\nCompiling classes from annotations and generated classes...");
				String libs = ProcessAnnotation.class.getResource("/libs").getPath();
				
				JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
				int opStats = jc.run(null, null, null, 
						"-d",Values.getCompilationPath().toString(), 
						"--module-path", libs,
						"-sourcepath", "/src/main/java/:" + Values.getOutputPackagePath(),
						Values.getExistingPJavaGeneratedSourcePath().toString());
				
				if(opStats == 0) {
					System.out.println("\nCompilation was successful!\n");
				} else {
					System.out.println("\nAn error occurred while compiling\n");
				}
			}).get();
		}
	}
}
