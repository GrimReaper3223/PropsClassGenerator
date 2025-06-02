package com.dsl.test.classgen;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.Generator;

class GeneratorTest {

	static Path inPropsPath = Path.of("/home/Deiv/workspace/git/SellerTree-Commercial/AdminManager/src/main/resources/values/strings");
	static String packageClass = "com.dsl.classgen";
	
	@BeforeAll
	@Disabled("Teste bem-sucedido")
	static void initTest() {
		Generator.init(inPropsPath, packageClass, false);
	}
	
	@Test
	@Disabled("Teste bem-sucedido")
	void generateTest() {
		Generator.generate();
	}
}
