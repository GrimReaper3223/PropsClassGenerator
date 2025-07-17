package com.dsl.classgen.generator;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.dsl.classgen.utils.Utils;

sealed interface Parsers permits InnerFieldGenerator, InnerStaticClassGenerator, OutterClassGenerator {

	default <T> String createAnnotation(Class<? extends Annotation> annotationClass, Map<String, T> elementMap) {
		StringBuilder sb = new StringBuilder();
		
		for(var elem : elementMap.entrySet()) {
			sb.append(elem.getKey()).append(" = ").append(
				switch(elem.getValue()) {
					case Integer i -> i + ", ";
					case String s -> String.format(!s.contains(".class") ? "\"%s\", " : "%s, ", s);
					default -> throw new IllegalArgumentException("Unexpected value: " + elem.getValue());
				}
			);
		}
		String annotationValues = sb.toString().trim();
		return String.format("@%s(%s)", annotationClass.getSimpleName(), annotationValues.substring(0, annotationValues.lastIndexOf(",")));
	}
	
	default String createImports(List<Class<? extends Annotation>> annotationClassList) {
		return annotationClassList.stream()
								  .map(cls -> "import " + cls.getName() + ";")
								  .collect(Collectors.joining("\n"));
	}
	
	default String parseClassName(Path propertiesFileName) {
		final String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
		String propertyFileName = Utils.formatFileName(propertiesFileName).toString();
		
		return Arrays.stream(propertyFileName.split(regex))
					 .map(token -> token.replaceFirst(Character.toString(token.charAt(0)),
					 		Character.toString(Character.toUpperCase(token.charAt(0)))))
					 .collect(Collectors.joining());
	}

	default String parseFieldName(String data) {
        String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
        fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
        return fieldName.toUpperCase();
    }
	
	default String parseFieldValue(String fieldValue, String propertiesDataType) {
        Predicate<String> testValue = propertiesDataType::equals;
        String pattern = testValue.test("String") ? "\"%s\"" : 
        				  testValue.test("char") || testValue.test("Character") ? "'%s'" : "%s";
        return String.format(pattern, fieldValue);
    }
}
