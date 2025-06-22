package com.dsl.test.classgen;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dsl.classgen.Generator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeneratorTestWithCacheErasure implements Tools {

	String beforeStartMessage = """
			%n%n#######################################
			#######################################
			### TEST %s ###
			#######################################
			#######################################%n%n
			""";
	
	List<String> messageList = List.of(
			"SINGLE FILE WITH CACHE ERASURE",
			"MANY FILES WITHOUT RECURSION AND WITH CACHE ERASURE",
			"MANY FILES WITH RECURSION AND WITH CACHE ERASURE");
	static int index = 0;
	
//	@BeforeEach
	void setup() {
		System.out.format(beforeStartMessage, messageList.get(index));
		eraseCache();
	}
	
	@Test
	@Order(1)
	@Disabled("Teste bem-sucedido")
	void generateSingleFileWithCacheErasure() {
		Generator.init(inPropsPath.resolve("exception-message.properties"), packageClass, false);
		Generator.generate();
		
		// prepara para o proximo test case
		index = 1;
	}
	
	@Test
	@Order(2)
	@Disabled("Teste bem-sucedido")
	void generateManyFilesWithoutRecursionAndWithCacheErasure() {
		Generator.init(inPropsPath, packageClass, false);
		Generator.generate();
		
		// prepara para o proximo test case
		index = 2;
	}
	
	@Test
	@Order(3)
	@Disabled("Teste bem-sucedido")
	void generateManyFilesWithRecursionAndWithCacheErasure() {
		Generator.init(inPropsPath, packageClass, true);
		Generator.generate();
	}
}