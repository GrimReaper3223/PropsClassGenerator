package com.dsl.classgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import com.dsl.classgen.engine.OutputWriter;
import com.dsl.classgen.parsers.ClassParser;

public class Generator {

	private static List<Path> pathQueue;
	private static boolean isSingleFile;
	
	private static final Properties PROPS = new Properties();
	private static String mainClassName = "P";
	
	private static String propertiesDataType;
	private static Path outputPath;
	
	private static String propertiesfileName;
	private static String packageOfGeneratedClass;
	private static String generatedClass;
	
	private static boolean isDebugMode = false;
	
	private static final String EXCEPTION_TXT = """
			Erro: Não foi encontrada a identificação do tipo de variável para criação das classes.
			Insira o tipo variável e tente novamente.
			De preferência, coloque o identificador no topo do arquivo para que ele possa ser analisado mais rapido.
			
			Ex.:
				# $javatype:@String
				
				... restante do arquivo .properties...
				
				# - Comentário para o arquivo de propriedade;
				$javatype - Identificador de tipo java;
				: - Separador sintático;
				@ - Avisa ao serviço que após este identificador, o tipo java será lido para a variável;
				String - O tipo java usado neste exemplo;
			""";
	
	private Generator() {}
	
	public static void init(Path inPath, Path outPath, String packageClass) {
		outputPath = outPath;
		packageOfGeneratedClass = packageClass;
		
		if(Files.isRegularFile(inPath)) {
			loadPropFile(inPath);
			isSingleFile = true;
			
		} else if(Files.isDirectory(inPath)) {
			try(Stream<Path> pathStream = Files.list(inPath)) {
				pathQueue = pathStream.filter(path -> !path.endsWith(".properties") && Files.isRegularFile(path))
						  			  .toList();
			} catch (IOException e) {
				e.printStackTrace();
				
			} finally {
				isSingleFile = false;
			}
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
	}
	
	public static void init(String inPropsPath, String outPath, String packageClass) {
		init(Path.of(inPropsPath), Path.of(outPath), packageClass);
	}
	
	public static void generate() {
		generatedClass = new ClassParser().parseClass();
		
		if(isDebugMode) {
			System.out.println(generatedClass);
		} else {
			try {
				OutputWriter.write();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// getters 
	public static Properties getPropertyObject() {
		return PROPS;
	}
	
	public static String getPropertiesDataType() {
		return propertiesDataType;
	}
	
	public static String getPackageOfGeneratedClass() {
		return packageOfGeneratedClass;
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
}
