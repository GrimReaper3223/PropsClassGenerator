package com.dsl.test.classgen;

import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dsl.classgen.Generator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests with erasure of generated data")
class TestGeneratorWithErasureOfGeneratedData implements HarnessTestTools {
	
	@BeforeEach
	void setup() {
		eraseGeneratedData();
	}
	
	@Test
	@Order(1)
	@DisplayName("Generate single file with erasure of generated data and not recursive")
	void generateSingleFileWithErasureOfGeneratedData() {
		Assertions.assertFalse(Files.exists(sourceDirPath));
		Generator.init(inPropsPath.resolve("exception-message.properties"), PACKAGE_CLASS, false);
		Generator.generate();
		Assertions.assertTrue(Files.exists(sourceDirPath));
	}
	
	@Test
	@Order(2)
	@DisplayName("Generate many files with erasure of generated data and not recursive")
	void generateManyFilesWithoutRecursionAndWithErasureOfGeneratedData() {
		Assertions.assertFalse(Files.exists(sourceDirPath));
		Generator.init(inPropsPath, PACKAGE_CLASS, false);
		Generator.generate();
		Assertions.assertTrue(Files.exists(sourceDirPath));
	}
	
	@Test
	@Order(3)
	@Disabled("Teste bem-sucedido")
	@DisplayName("Generate many files with erasure of generated data and recursive")
	void generateManyFilesWithRecursionAndWithErasureOfGeneratedData() {
		Assertions.assertFalse(Files.exists(sourceDirPath));
		Generator.init(inPropsPath, PACKAGE_CLASS, true);
		Generator.generate();
		Assertions.assertTrue(Files.exists(sourceDirPath));
	}
}