package com.dsl.classgen;

import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.generators.OutterClassGenerator;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.ProcessQueuedFileEvents;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.file_manager.Compiler;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {
	
	private static final Logger LOGGER = LogManager.getLogger(Generator.class);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	
	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		// verifica se a estrutura ja esta gerada
		new GeneratedStructureChecker().checkFileSystem();
		
		// define e resolve alguns dados
		flagsCtx.setIsRecursive(isRecursive);
		pathsCtx.setInputPropertiesPath(inputPath);
		pathsCtx.setPackageClass(Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString());
		pathsCtx.resolvePaths(pathsCtx.getPackageClass());
		
		// le o caminho passado e processa o cache
		Reader.read(inputPath);
		CacheManager.processCache();
		
		LOGGER.log(Level.INFO, """
				
				-----------------------------
				--- Framework Initialized ---
				-----------------------------
				
				Input Path: {};
				Output Directory Path: {};
				Package Class: {};
				Is Recursive?: {};
				Is Single File?: {};
				Is There a Generated Structure?: {};
				
				Developer Options
				Is Debug Mode?: {};
				
				-----------------------------
				-----------------------------
				
				Call 'Generator.generate()' to generate java classes or parse existing classes.
				
				""", inputPath, 
					 pathsCtx.getOutputSourceDirPath(), 
					 pathsCtx.getPackageClass(), 
					 isRecursive, 
					 flagsCtx.getIsSingleFile(),
					 flagsCtx.getIsDirStructureAlreadyGenerated(), 
					 flagsCtx.getIsDebugMode());
	}

	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}

	public static void generate() {
		// inicia a geracao se a estrutura de diretorios ou o arquivo final P.java nao existir
		// do contrario, deve efetuar o processamento do que ja existe
		if (!flagsCtx.getIsDirStructureAlreadyGenerated() || !flagsCtx.getIsExistsPJavaSource()) {
			Utils.calculateElapsedTime();
			new OutterClassGenerator().generateOutterClass();
			
			if(flagsCtx.getIsDebugMode()) {
				LOGGER.log(Level.DEBUG, pathsCtx.getGeneratedClass());
			} 
			Writer.write();
				
		} else {
			LOGGER.log(Level.WARN, """
					There is already a generated structure.
					
					Generating additional classes and checking the existing ones...
					""");
		}
		// compila a classe gerada, inicializa o servico de monitoramento de diretorios e processa as anotacoes da classe gerada/existente
		Compiler.compile();
		WatchServiceImpl.initialize();
		ProcessQueuedFileEvents.initialize();
		ProcessAnnotation.processAnnotations();
	}
}
