package com.dsl.classgen;

import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.generators.OutterClassGenerator;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.ProcessQueuedFileEvents;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.cache_system.FileCacheSystem;
import com.dsl.classgen.io.file_handler.Compiler;
import com.dsl.classgen.io.file_handler.Reader;
import com.dsl.classgen.io.file_handler.Writer;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {
	
	private static final Logger LOGGER = LogManager.getLogger(Generator.class);
	
	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		// verifica se a estrutura ja esta gerada
		GeneratedStructureChecker.checkGeneratedStructure();
		
		// define e resolve alguns dados
		Values.setIsRecursive(isRecursive);
		Values.setInputPropertiesPath(inputPath);
		Values.setPackageClass(Utils.normalizePath(packageClass.concat(".generated"), "/", ".").toString());
		Values.resolvePaths();
		
		// le o caminho passado e processa o cache
		Reader.read(inputPath);
		FileCacheSystem.processCache();
		
		LOGGER.log(Level.INFO, """
				\n-----------------------------
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
				
				Call 'Generator.generate()' to generate java classes or parse existing classes.\n
				""", inputPath, 
					 Values.getOutputSourceDirPath(), 
					 Values.getPackageClass(), 
					 isRecursive, 
					 Values.isSingleFile(),
					 Values.isDirStructureAlreadyGenerated(), 
					 Values.isDebugMode());
	}

	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}

	public static void generate() {
		// inicia a geracao se a estrutura de diretorios ou o arquivo final P.java nao existir
		// do contrario, deve efetuar o processamento do que ja existe
		if (!Values.isDirStructureAlreadyGenerated() || !Values.isExistsPJavaSource()) {
			Utils.calculateElapsedTime();
			new OutterClassGenerator().generateOutterClass();
			
			if(Values.isDebugMode()) {
				System.out.println(Values.getGeneratedClass());
			} else {
				Writer.write();
			}
				
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
