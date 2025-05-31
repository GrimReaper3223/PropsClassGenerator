package com.dsl.classgen.parsers;

sealed interface Commons permits ClassParser, InnerFieldParser, InnerStaticClassParser {

	default String formatAnnotationClassName(Class<?> cls) {
		String className = cls.getName();
		return className.substring(className.lastIndexOf('.') + 1);
	}
}
