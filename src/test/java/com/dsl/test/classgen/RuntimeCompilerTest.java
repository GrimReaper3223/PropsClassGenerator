package com.dsl.test.classgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.io.Values;

class RuntimeCompilerTest {
	
	@Test
	@Disabled("Teste bem-sucedido")
	void executeRuntimeCompiler() throws ClassNotFoundException, InterruptedException {
		String libs = getClass().getResource("/libs").getPath();
		assertTrue(libs.contains("libs"));
		
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		jc.run(null, null, null, 
				"-d","target/classes",
				"--module-path", libs,
				"-sourcepath", "/src/main/java/:" + Values.getOutputPackagePath(),
				Values.getExistingPJavaGeneratedSourcePath().toString());
	}
}