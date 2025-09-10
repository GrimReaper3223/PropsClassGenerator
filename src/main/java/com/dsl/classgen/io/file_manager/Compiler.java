package com.dsl.classgen.io.file_manager;

import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.module.ModuleFinder;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.tools.ToolProvider;

import com.dsl.classgen.io.StructureChecker;
import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.synchronizer.SyncBin;
import com.dsl.classgen.utils.LogLevels;

public final class Compiler extends SupportProvider {

	private Compiler() {}

    public static void compile() {
    	// a compilacao ocorre caso nao exista a classe P.java compilada. Do contrario, o metodo apenas retorna
        if (!flagsCtx.getIsExistsCompiledPJavaClass()) {
        	LOGGER.log(LogLevels.NOTICE.getLevel(), "Compiling classes from annotations and generated classes...\n");

            int opStats = ToolProvider.getSystemJavaCompiler().run(null, null, null,
            		"-d", pathsCtx.getOutputClassFilePath().toString(),
        			"--module-path", locateLoadedModules(),
        			"-sourcepath", pathsCtx.getOutputSourceDirPath().toString(),
            		pathsCtx.getExistingPJavaGeneratedSourcePath().toString());

            if(opStats == 0) {
            	LOGGER.log(LogLevels.SUCCESS.getLevel(), "Compilation was successful!\n");
            	// verifica novamente o sistema de arquivos para atualizar variaveis
            	StructureChecker.checkStructure();
            	LOGGER.warn("Add package export clause for package '{}' in 'module-info.java' file to avoid unnamed module access issues.", pathsCtx.getPackageClass());
            } else {
            	LOGGER.error("An error occurred while compiling\n");
            }
        }
    }

	private static String locateLoadedModules() {
		String osName = System.getProperty("os.name").toLowerCase();
		return ModuleLayer.boot().configuration().modules().stream()
				.map(module -> module.reference().location().get().getPath())
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					list.sort(Comparator.comparing(path -> !Files.isRegularFile(Path.of(path))));
					return list.stream().collect(
							Collectors.joining(osName.contains("linux") || osName.contains("unix") ? ":" : ";"));
				}));
	}

	/*
	 * Automatically inserts a module attribute for the P.class class when
	 * generating all files for the first time. The developer deploying this
	 * framework should be responsible for inserting the 'exports *.generated;'
	 * directive in their module-info.java and then running a build so that
	 * module-info.class has the new 'exports' directive values.
	 */
    @SuppressWarnings("unused")
	private static void exportGeneratedPackageInCurrentModule() {
    	SyncBin syncBin = new SyncBin();
    	ModuleFinder mf = ModuleFinder.of(Path.of("target/classes"));
    	ModuleDesc moduleDesc = ModuleDesc.of(mf.findAll().stream().map(ref -> ref.descriptor().name()).limit(1).collect(Collectors.joining()));
    	PackageDesc pkgDesc = PackageDesc.of(pathsCtx.getPackageClass());
    	ModuleExportInfo mei = ModuleExportInfo.of(pkgDesc, AccessFlag.MODULE.mask(), moduleDesc);
    	ModuleAttribute moduleAttr = ModuleAttribute.of(moduleDesc, handler -> handler.exports(mei));
    	syncBin.insertModuleToOutterClass(moduleAttr);
    }
}