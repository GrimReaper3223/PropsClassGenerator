package com.dsl.classgen.models;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Parsers{

	@Deprecated
	default String createAnnotation(Class<? extends Annotation> annotationClass, Map<String, ?> annotationModelMap) {
		return String.format("@%s(%s)", annotationClass.getSimpleName(),
				annotationModelMap.entrySet()
								  .stream()
								  .map(entry -> String.format("%s = %s", entry.getKey(), entry.getValue()))
								  .collect(Collectors.joining(", ")));
	}
	
	@Deprecated
	static String createImports(List<Class<? extends Annotation>> annotationClassList) {
		return annotationClassList.stream()
								  .map(cls -> "import " + cls.getName() + ";")
								  .collect(Collectors.joining("\n"));
	}
	
	default <T> String parseClassName(T propertiesFilePath) {
		final String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
		String propertyFileName = Path.of(propertiesFilePath.toString()).getFileName().toString();
		String formattedPropertyFileName = propertyFileName.substring(0, propertyFileName.lastIndexOf("."));
		
		return Arrays.stream(formattedPropertyFileName.split(regex))
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
        Predicate<String> testValue = propertiesDataType::contains;
        String pattern = testValue.test("String") ? "\"%s\"" : 
        				  testValue.test("char") || testValue.test("Character") ? "'%s'" : "%s";
        return String.format(pattern, fieldValue);
    }
}
