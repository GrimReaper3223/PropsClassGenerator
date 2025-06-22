package com.dsl.classgen;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.generators.OutterClassGenerator;
import com.dsl.classgen.io.Compiler;
import com.dsl.classgen.io.FileCacheSystem;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.Reader;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.Writer;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;
import java.nio.file.Path;

public final class Generator {
	
	private Generator() {}

	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		// verifica se a estrutura ja esta gerada
		GeneratedStructureChecker.checkGeneratedStructure();
		
		// define e resolve alguns dados
		Values.setIsRecursive(isRecursive);
		Values.setInputPropertiesPath(inputPath);
		Values.setPackageClass(packageClass.concat(".generated"));
		Values.resolvePaths();
		
		// le o caminho passado e processa o cache
		Reader.read(inputPath);
		FileCacheSystem.processCache();
		
		// verifica se ja existe a classe P.java compilada
		GeneratedStructureChecker.checkIfExistsCompiledClass();
		
		System.out.format("""
				%n-----------------------------
				--- Framework Initialized ---
				-----------------------------
				
				Input Path: %s;
				Output Directory Path: %s;
				Package Class: %s;
				Is Recursive?: %b;
				Is Single File?: %b;
				Is There a Generated Structure?: %b;
				
				Developer Options
				Is Debug Mode?: %b;
				
				-----------------------------
				-----------------------------
				
				Call 'Generator.generate()' to generate java classes or parse existing classes.%n
				""", inputPath, 
					 Values.getOutputPackagePath(), 
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
			
			if (Values.isDebugMode()) {
				System.out.println(Values.getGeneratedClass());
			} else {
				Writer.write();
			}
		} else {
			System.out.println("""
					\nThere is already a generated structure.
					
					Generating additional classes and checking the existing ones...\n
					""");
		}
		// compila a classe gerada, inicializa o servico de monitoramento de diretorios e processa as anotacoes da classe gerada/existente
		Compiler.compile();
		WatchServiceImpl.initialize();
		ProcessAnnotation.processAnnotations();
	}
}
