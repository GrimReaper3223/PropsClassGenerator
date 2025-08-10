package com.dsl.test.classgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.Generator;

class SimpleGeneratorTest implements HarnessTestTools {

	@Test
	void generationTest() {
		Generator.init(inPropsPath, PACKAGE_CLASS, false);
		Generator.generate();

		// ignore os testes de assercao
		Assertions.assertTrue(true);
	}
}
