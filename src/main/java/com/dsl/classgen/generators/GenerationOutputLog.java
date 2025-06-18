package com.dsl.classgen.generators;

import java.util.Objects;

sealed interface GenerationOutputLog permits OutterClassGenerator, InnerFieldGenerator, InnerStaticClassGenerator {

	default void formatGenerationOutput(String input1, String input2, String input3) {
		System.out.format("%nGenerating %s '%s' %s", input1, input2, Objects.isNull(input3) ? "" : "[" + input3 + "]");
	}
}
