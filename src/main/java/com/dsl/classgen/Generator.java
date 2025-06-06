package com.dsl.classgen;

import static com.dsl.classgen.io.Values.getGeneratedClass;
import static com.dsl.classgen.io.Values.getOutputPath;
import static com.dsl.classgen.io.Values.getPackageClass;
import static com.dsl.classgen.io.Values.isDebugMode;
import static com.dsl.classgen.io.Values.isSingleFile;
import static com.dsl.classgen.io.Values.hasStructureAlreadyGenerated;
import static com.dsl.classgen.io.Values.setGeneratedClass;
import static com.dsl.classgen.io.Values.setInputPath;
import static com.dsl.classgen.io.Values.setIsRecursive;
import static com.dsl.classgen.io.Values.setPackageClass;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.FileCacheSystem;
import com.dsl.classgen.io.GeneratedStructureChecker;
import com.dsl.classgen.io.Reader;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.Writer;
import com.dsl.classgen.parsers.ClassParser;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {

	static {
		try {
			Values.setHasStructureAlreadyGenerated(GeneratedStructureChecker.checkGeneratedStructure());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private Generator() {}
	
	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		setIsRecursive(isRecursive);
		setInputPath(inputPath);
		Reader.read(inputPath);
		
		if(!Values.hasStructureAlreadyGenerated()) {
			setPackageClass(packageClass);
			Values.resolvePaths();
		}
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
				""", inputPath, getOutputPath(), getPackageClass(), isRecursive, isSingleFile(), hasStructureAlreadyGenerated(), isDebugMode());
	}
	
	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}
	
	public static void generate() {
		if(!Values.hasStructureAlreadyGenerated()) {
			Utils.calculateElapsedTime();
			setGeneratedClass(new ClassParser().parseClass());
		
			if(isDebugMode()) {
				System.out.println(getGeneratedClass());
			} else {
				try {
					Writer.write();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.err.println("Interrupting Thread...");
					Thread.currentThread().interrupt();
				}
			}
		} else {
			System.out.println("""
					There is already a generated structure.
					
					Generating additional classes and checking the existing ones...
					""");
		}
		WatchServiceImpl.initialize();
		FileCacheSystem.processCache();
	}
}
