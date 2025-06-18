package com.dsl.classgen;

import static com.dsl.classgen.io.Values.isDirStructureAlreadyGenerated;
import static com.dsl.classgen.io.Values.getGeneratedClass;
import static com.dsl.classgen.io.Values.getOutputPackagePath;
import static com.dsl.classgen.io.Values.getPackageClass;
import static com.dsl.classgen.io.Values.isDebugMode;
import static com.dsl.classgen.io.Values.isSingleFile;
import static com.dsl.classgen.io.Values.setInputPropertiesPath;
import static com.dsl.classgen.io.Values.setIsRecursive;
import static com.dsl.classgen.io.Values.setPackageClass;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

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

public final class Generator {

	static {
		// verifica se a estrutura de dados do framework ja foi gerada
		GeneratedStructureChecker.checkGeneratedStructure();
	}
	
	private Generator() {}
	
	// se a estrutura de dados ja estiver presente, 
	// devemos ainda setar a recursao, a entrada do arquivo de propriedades
	// pois pode ser um arquivo novo ainda nao mapeado
	// 
	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		// define algumas propriedades
		setIsRecursive(isRecursive);
		setInputPropertiesPath(inputPath);
		setPackageClass(packageClass.concat(".generated"));		// src/main/java/ + packageClass + .generated 
		
		// le o arquivo no caminho passado
		Reader.read(inputPath);
		
		// resolve todos os caminhos em caminhos finais utilizaveis pelo framework
		Values.resolvePaths();
		
		// inicia a estrutura contendo o cache ja gerado (se existir)
		FileCacheSystem.processCache();
		
		System.out.format("""
				-----------------------------
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
				
				Call 'Generator.generate()' to generate java classes or parse existing classes.
				""", inputPath, getOutputPackagePath(), getPackageClass(), isRecursive, isSingleFile(), isDirStructureAlreadyGenerated(), isDebugMode());
		
		GeneratedStructureChecker.checkIfExistsCompiledClass();
	}
	
	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}
	
	public static void generate() {
		try {
			if(!Values.isDirStructureAlreadyGenerated() || !Values.isExistsPJavaGeneratedSourcePath()) {
				Utils.calculateElapsedTime();
				new OutterClassGenerator().generateOutterClass();
			
				if(isDebugMode()) {
					System.out.println(getGeneratedClass());
				} else {
					Writer.write();
					Compiler.compile();
				}
			} else {
				System.out.println("""
						\nThere is already a generated structure.
						
						Generating additional classes and checking the existing ones...
						""");
			}
			WatchServiceImpl.initialize();
			ProcessAnnotation.processAnnotations();
			
		} catch (ClassNotFoundException | ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Interrupting Thread...");
			Thread.currentThread().interrupt();
		}
	}
}
