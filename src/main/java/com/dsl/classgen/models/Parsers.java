package com.dsl.classgen.models;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The Interface Parsers.
 */
public interface Parsers {

	/**
	 * Parses the class name. Receives the path of the properties file to be mapped
	 * and converts the file name to the name of a class, according to Java
	 * conventions.
	 *
	 * @param <T>                the generic type to be associated with the argument
	 *                           (String or Path)
	 * @param propertiesFilePath the properties file path
	 * @return the string containing the formatted class name
	 */
	default <T> String parseClassName(@NonNull T propertiesFilePath) {
		final String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
		String propertyFileName = Optional.of(Path.of(propertiesFilePath.toString()).getFileName()).orElseThrow().toString();
		String formattedPropertyFileName = propertyFileName.substring(0, propertyFileName.lastIndexOf("."));

		return Arrays.stream(formattedPropertyFileName.split(regex))
				.map(token -> token.replaceFirst(Character.toString(token.charAt(0)),
						Character.toString(Character.toUpperCase(token.charAt(0)))))
				.collect(Collectors.joining());
	}

	/**
	 * Parses the field name. Receives the key of a property to be mapped and
	 * converts the key name to the name of a constant field, according to Java
	 * conventions.
	 *
	 * @param data the mapped key of the currently loaded properties file
	 * @return the string containing the formatted field name
	 */
	default String parseFieldName(String data) {
		String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
		fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
		return fieldName.toUpperCase();
	}

	/**
	 * Parses the field value. Receives the value associated with the key that was
	 * mapped from a properties file and formats this value according to the Java
	 * type assigned in the mapped properties file.
	 *
	 * @param fieldValue         the field value
	 * @param propertiesDataType the properties data type
	 * @return the string containing the formatted field value
	 */
	default String parseFieldValue(String fieldValue, String propertiesDataType) {
		Predicate<String> testValue = propertiesDataType::contains;

		boolean stringValueParsingRequired = !fieldValue.startsWith("\"") && !fieldValue.endsWith("\"");
		boolean charValueParsingRequired = !fieldValue.startsWith("'") && !fieldValue.endsWith("'");
		boolean stringTest = testValue.test("String");
		boolean charTest = testValue.test("char") || testValue.test("Character");

		String pattern = stringTest && stringValueParsingRequired ? "\"%s\""
				: charTest && charValueParsingRequired ? "'%s'" : "%s";
		return String.format(pattern, fieldValue);
	}
}
