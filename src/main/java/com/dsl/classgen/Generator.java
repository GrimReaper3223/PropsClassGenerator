package com.dsl.classgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.dsl.classgen.engine.OutputWriter;
import com.dsl.classgen.parsers.ClassParser;

public class Generator {

	private static List<Path> pathQueue = new ArrayList<>();
	private static boolean isSingleFile;
	private static boolean isRecursive = false;
	
	private static final Properties PROPS = new Properties();
	private static String mainClassName = "P";
	
	private static String propertiesDataType;
	private static Path outputPath;
	
	private static String propertiesfileName;
	private static String packageClass;
	private static String generatedClass;
	
	private static boolean isDebugMode = false;
	
	public static long startTimeOperation = 0L;
	public static long endTimeOperation = 0L;
	
	private static final String EXCEPTION_TXT = """
			Error: The variable type identification was not found for creating the classes.
			Enter the variable type and try again.
			Preferably, place the identifier at the top of the file so that it can be analyzed faster.
			
			Ex.:
			# $javatype:@String
			
			... rest of the .properties file...
			
			# - Comment for the property file;
			$javatype - Java type identifier;
			: - Syntactic separator;
			@ - Tells the service that after this identifier, the java type will be read for the variable;
			String - The java type used in this example;
			""";
	
	private Generator() {}
	
	public static void init(Path inputPath, Path outputPath, String packageClass, boolean isRecursive) {
		Generator.outputPath = outputPath;
		Generator.packageClass = packageClass;
		Generator.isRecursive = isRecursive;
		
		if(Files.isRegularFile(inputPath)) {
			loadPropFile(inputPath);
			isSingleFile = true;
			
		} else if(Files.isDirectory(inputPath)) {
			Predicate<Path> testPath = path -> {
				String stringPath = path.getFileName().toString();
				return stringPath.substring(stringPath.indexOf('.')).endsWith(".properties");
			};
			
			try {
				if(isRecursive) {
					Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if(testPath.test(file)) {
								pathQueue.add(file);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				} else {
					try(Stream<Path> pathStream = Files.list(inputPath)) {
						pathQueue = pathStream.filter(testPath::test)
											  .filter(Files::isRegularFile)
											  .toList();
					}
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isSingleFile = false;
			}
			
			System.out.format("""
					-----------------------------
					--- Framework Initialized ---
					-----------------------------
					
					Properties File Path: %s;
					Output Directory Path: %s;
					Package Class: %s;
					Is Recursive?: %b;
					Is Single File?: %b;
					
					Developer Options:
					Is Debug Mode?: %b;
					
					-----------------------------
					-----------------------------
					
					call 'Generator.generate()' to generate the java classes.
					""", inputPath, outputPath, packageClass, isRecursive, isSingleFile, isDebugMode);
		}
	}
	
	public static void loadPropFile(Path inPath) {
		try(InputStream in = Files.newInputStream(inPath);
				Stream<String> lines = Files.lines(inPath, StandardCharsets.ISO_8859_1)) {
			if(!PROPS.isEmpty()) {
				PROPS.clear();
			}
			PROPS.load(in);
			
			propertiesDataType = lines.filter(input -> input.contains("$javatype:"))
									  .findFirst()
									  .map(input -> input.substring(input.indexOf('@') + 1))
									  .orElseThrow(() -> new IOException(EXCEPTION_TXT));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		propertiesfileName = inPath.getFileName().toString();
		propertiesfileName = propertiesfileName.substring(0, propertiesfileName.lastIndexOf('.'));
		
		System.out.format("%n%n***Properties file loaded from path: %s***%n", inPath);
	}
	
	public static void init(String inPropsPath, String outPath, String packageClass, boolean isRecursive) {
		init(Path.of(inPropsPath), Path.of(outPath), packageClass, isRecursive);
	}
	
	public static void generate() {
		calculateElapsedTime();
		System.out.println("\nGenerating classes...");
		generatedClass = new ClassParser().parseClass();
		
		if(isDebugMode) {
			System.out.println(generatedClass);
		} else {
			try {
				System.out.println("\n\nWriting data...");
				OutputWriter.write();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static long calculateElapsedTime() {
		if(startTimeOperation == 0L) {
			startTimeOperation = System.currentTimeMillis();
			return 0L;
		}
		
		if(endTimeOperation == 0L) {
			endTimeOperation = System.currentTimeMillis();
		}
		
		return endTimeOperation - startTimeOperation;
	}
	
	// getters 
	public static Properties getPropertyObject() {
		return PROPS;
	}
	
	public static String getPropertiesDataType() {
		return propertiesDataType;
	}
	
	public static String getPackageOfGeneratedClass() {
		return packageClass;
	}

	public static List<Path> getPathList() {
		return pathQueue;
	}
	
	public static Path getOutputPath() {
		return outputPath;
	}
	
	public static String getPropertiesFileName() {
		return propertiesfileName;
	}
	
	public static String getOutterClassName() {
		return mainClassName;
	}
	
	public static String getGeneratedClass() {
		return generatedClass;
	}
	
	public static boolean isSingleFile() {
		return isSingleFile;
	}
	
	public static boolean isRecursiveAction() {
		return isRecursive;
	}
}
