package com.dsl.test.classgen;

import org.junit.jupiter.api.Test;

import com.dsl.classgen.Generator;

class SimpleGeneratorTest implements Tools {
	
	@Test
	void generationTest() {
		Generator.init(inPropsPath, packageClass, false);
		Generator.generate();
	}
}
