package com.dsl.test.classgen;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dsl.classgen.Generator;
import com.dsl.classgen.io.GeneratedStructureChecker;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeneratorTest {

	static Path inPropsPath = Path.of("/home/Deiv/workspace/git/SellerTree-Commercial/AdminManager/src/main/resources/values/strings/");
	static String packageClass = "com.dsl.classgen";
	
	@BeforeAll
	static void initTest() {
		Generator.init(inPropsPath, packageClass, true);
	}
	
	@Test
	@Order(1)
	@Disabled("Teste bem-sucedido")
	void generateTest() {
		Generator.generate();
	}
	
	@Test
	@Order(2)
	@Disabled("Teste bem-sucedido")
	void isExistsPackage() throws InterruptedException, ExecutionException {
		Assertions.assertTrue(GeneratedStructureChecker.checkGeneratedStructure());
	}
}
