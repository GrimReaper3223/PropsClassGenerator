package com.dsl.classgen.io.file_manager;

import javax.tools.ToolProvider;

import com.dsl.classgen.io.StructureChecker;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.utils.LogLevels;

public final class Compiler extends SupportProvider {

	private Compiler() {}

    public static void compile() {
    	// a compilacao ocorre caso nao exista a classe P.java compilada. Do contrario, o metodo apenas retorna
        if (!flagsCtx.getIsExistsCompiledPJavaClass()) {
        	LOGGER.log(LogLevels.NOTICE.getLevel(), "Compiling classes from annotations and generated classes...\n");

            int opStats = ToolProvider.getSystemJavaCompiler().run(null, null, null,
            		"-d", pathsCtx.getOutputClassFilePath().toString(),
            		"--module-path", Compiler.class.getResource("/libs").getPath(),
            		"-sourcepath", "/src/main/java/:" + pathsCtx.getOutputSourceDirPath().toString(),
            		pathsCtx.getExistingPJavaGeneratedSourcePath().toString());
            if(opStats == 0) {
            	LOGGER.log(LogLevels.SUCCESS.getLevel(), "Compilation was successful!\n");
            } else {
            	LOGGER.error("An error occurred while compiling\n");
            }

            // verifica novamente o sistema de arquivos para atualizar variaveis
            StructureChecker.checkStructure();
        }
    }
}

