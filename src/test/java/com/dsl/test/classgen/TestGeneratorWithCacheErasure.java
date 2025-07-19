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
@DisplayName("Tests with cache erasure")
class TestGeneratorWithCacheErasure implements HarnessTestTools {

	@BeforeEach
	void setup() {
		eraseCache();
	}
	
	@Test
	@Order(1)
	@DisplayName("Generate single file with cache erasure and without recursion")
	void generateSingleFileWithCacheErasure() {
		Assertions.assertFalse(Files.exists(cachePath));
		Generator.init(inPropsPath.resolve("exception-message.properties"), PACKAGE_CLASS, false);
		Generator.generate();
		Assertions.assertTrue(Files.exists(sourceDirPath));
	}
	
	@Test
	@Order(2)
	@DisplayName("Generate many files with cache erasure and without recursion")
	void generateManyFilesWithCacheErasure() {
		Assertions.assertFalse(Files.exists(cachePath));
		Generator.init(inPropsPath, PACKAGE_CLASS, false);
		Generator.generate();
		Assertions.assertTrue(Files.exists(sourceDirPath));
	}
	
	@Test
	@Order(3)
	@Disabled("Teste bem-sucedido")
	@DisplayName("Generate many files with cache erasure and with recursion")
	void generateManyFilesWithRecursionAndWithCacheErasure() {
		Assertions.assertFalse(Files.exists(cachePath));
		Generator.init(inPropsPath, PACKAGE_CLASS, true);
		Generator.generate();
	}
}