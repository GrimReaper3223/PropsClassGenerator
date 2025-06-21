package com.dsl.classgen;

import static com.dsl.classgen.io.Values.getGeneratedClass;
import static com.dsl.classgen.io.Values.getIsDebugMode;
import static com.dsl.classgen.io.Values.getIsSingleFile;
import static com.dsl.classgen.io.Values.getOutputPath;
import static com.dsl.classgen.io.Values.getPackageClass;
import static com.dsl.classgen.io.Values.setGeneratedClass;
import static com.dsl.classgen.io.Values.setInputPath;
import static com.dsl.classgen.io.Values.setIsRecursive;
import static com.dsl.classgen.io.Values.setPackageClass;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.Reader;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.Writer;
import com.dsl.classgen.parsers.ClassParser;
import com.dsl.classgen.services.WatchServiceImpl;
import com.dsl.classgen.utils.Utils;

public final class Generator {

	private Generator() {}
	
	public static void init(Path inputPath, String packageClass, boolean isRecursive) {
		setPackageClass(packageClass);
		setIsRecursive(isRecursive);
		setInputPath(inputPath);
		
		Reader.read(inputPath);
		Values.resolvePaths();
			
		System.out.format("""
				-----------------------------
				--- Framework Initialized ---
				-----------------------------
				
				Input Path: %s;
				Output Directory Path: %s;
				Package Class: %s;
				Is Recursive?: %b;
				Is Single File?: %b;
				
				Developer Options
				Is Debug Mode?: %b;
				
				-----------------------------
				-----------------------------
				
				call 'Generator.generate()' to generate the java classes.
				""", inputPath, getOutputPath(), getPackageClass(), isRecursive, getIsSingleFile(), getIsDebugMode());
	}
	
	public static void init(String inputPath, String packageClass, boolean isRecursive) {
		init(Path.of(inputPath), packageClass, isRecursive);
	}
	
	public static void generate() {
		Utils.calculateElapsedTime();
		setGeneratedClass(new ClassParser().parseClass());
		
		if(getIsDebugMode()) {
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
		WatchServiceImpl.initialize();
	}
}
