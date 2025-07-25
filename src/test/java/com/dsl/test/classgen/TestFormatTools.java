package com.dsl.test.classgen;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.Generator;
import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.utils.Utils;

class TestFormatTools implements HarnessTestTools {

	@Test
	@Disabled
	void testAnnotationCreation() {
		Assertions.assertDoesNotThrow(() -> createAnnotation(GeneratedInnerStaticClass.class, Map.of("key", "test", "hash", 12345)));
		Assertions.assertDoesNotThrow(() -> createAnnotation(GeneratedInnerStaticClass.class, Map.of("value", 12)));
		Assertions.assertDoesNotThrow(() -> createAnnotation(GeneratedInnerField.class, Map.of("key", "test", "hash", 12345, "value", "String123")));
	}
	
	<T> String createAnnotation(Class<? extends Annotation> annotationClass, Map<String, T> elementSet) {
		StringBuilder sb = new StringBuilder();
		
		for(var elem : elementSet.entrySet()) {
			sb.append(elem.getKey()).append(" = ").append(
				switch(elem.getValue()) {
					case Integer i -> i + ", ";
					case String s -> String.format("\"%s\", ", s);
					default -> throw new IllegalArgumentException("Unexpected value: " + elem.getValue());
				}
			);
		}
		String annotationValues = sb.toString().trim();
		return String.format("@%s(%s)", annotationClass.getSimpleName(), annotationValues.substring(0, annotationValues.lastIndexOf(",")));
	}
	
	@Test
	void testPathConverter() throws ClassNotFoundException {
		Generator.init(inPropsPath, PACKAGE_CLASS, true);
		Generator.generate();
		System.out.println(Utils.convertSourcePathToClassPath(inPropsPath.resolve("fx/fx-button.properties")));
	}
}
