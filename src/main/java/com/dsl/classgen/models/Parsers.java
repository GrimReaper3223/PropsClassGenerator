package com.dsl.classgen.models;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Parsers{

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
        
        boolean stringValueParsingRequired = !fieldValue.startsWith("\"") && !fieldValue.endsWith("\"");
        boolean charValueParsingRequired = !fieldValue.startsWith("'") && !fieldValue.endsWith("'");
        boolean stringTest = testValue.test("String");
        boolean charTest = testValue.test("char") || testValue.test("Character");
        
        String pattern = stringTest && stringValueParsingRequired ? "\"%s\"" : 
        				   charTest && charValueParsingRequired ? "'%s'" : "%s";
        return String.format(pattern, fieldValue);
    }
}
