package com.dsl.classgen.io.file_manager;

import javax.tools.ToolProvider;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.LogLevels;

/**
 * The Class Compiler.
 */
public final class Compiler extends SupportProvider {

	private Compiler() {}

	/**
	 * Compiles the source file P.java
	 */
	public static void compile() {
		if (!flagsCtx.getIsExistsCompiledPJavaClass()) {
			LOGGER.log(LogLevels.NOTICE.getLevel(), "Compiling classes from annotations and generated classes...\n");

			int opStats = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d",
					pathsCtx.getOutputClassFilePath().toString(), "--module-path",
					AnnotationProcessor.class.getResource("/libs").getPath(), "-sourcepath",
					"/src/main/java/:" + pathsCtx.getOutputSourceDirPath().toString(),
					pathsCtx.getExistingPJavaGeneratedSourcePath().toString());
			if (opStats == 0) {
				LOGGER.log(LogLevels.SUCCESS.getLevel(), "Compilation was successful!\n");
			} else {
				LOGGER.error("An error occurred while compiling\n");
			}

			new GeneratedStructureChecker().checkFileSystem();
		}
	}
}
