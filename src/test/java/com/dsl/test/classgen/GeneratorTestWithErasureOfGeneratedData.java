package com.dsl.test.classgen;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dsl.classgen.Generator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeneratorTestWithErasureOfGeneratedData implements Tools {
	
	Path inPropsPath = Path.of("/home/Deiv/workspace/git/SellerTree-Commercial/AdminManager/src/main/resources/values/strings/");
	String packageClass = "com.dsl.classgen";
	
	String beforeStartMessage = """
			%n%n#######################################
			#######################################
			### TEST %s ###
			#######################################
			#######################################%n%n
			""";
	
	List<String> messageList = List.of(
			"SINGLE FILE WITH GENERATED DATA ERASURE",
			"MANY FILES WITHOUT RECURSION AND WITH GENERATED DATA ERASURE",
			"MANY FILES WITH RECURSION AND WITH GENERATED DATA ERASURE");
	static int counter = 0;
	
	@BeforeEach
	void setup() {
		System.out.format(beforeStartMessage, messageList.get(counter));
		eraseGeneratedData();
		
		// revalidacao de cache. se isso nao for feito, a regeneracao pode causar problemas
		// TODO: criar revalidador de cache
		eraseCache();
	}
	
	@Test
	@Order(1)
	@Disabled("Teste bem-sucedido")
	void generateSingleFileWithErasureOfGeneratedData() {
		Generator.init(inPropsPath.resolve("exception-message.properties"), packageClass, false);
		Generator.generate();
		
		// prepara para o proximo test case
		counter = 1;
	}
	
	@Test
	@Order(2)
	@Disabled("Teste bem-sucedido")
	void generateManyFilesWithoutRecursionAndWithErasureOfGeneratedData() {
		Generator.init(inPropsPath, packageClass, false);
		Generator.generate();
		
		// prepara para o proximo test case
		counter = 2;
	}
	
	@Test
	@Order(3)
	@Disabled("Teste bem-sucedido")
	void generateManyFilesWithRecursionAndWithErasureOfGeneratedData() {
		Generator.init(inPropsPath, packageClass, true);
		Generator.generate();
	}
}
