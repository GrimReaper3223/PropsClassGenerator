package com.dsl.classgen.parsers;

import java.util.Objects;

sealed interface DataFormatter permits ClassParser, InnerFieldParser, InnerStaticClassParser {

	default String formatAnnotationClassName(Class<?> cls) {
		String className = cls.getName();
		return className.substring(className.lastIndexOf('.') + 1);
	}
	
	default void formatConsoleOutput(String input1, String input2, String input3) {
		System.out.format("%nGenerating %s '%s' %s", input1, input2, Objects.isNull(input3) ? "" : "[" + input3 + "]");
	}
}
