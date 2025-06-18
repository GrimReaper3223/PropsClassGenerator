package com.dsl.classgen.generators;

import static com.dsl.classgen.io.Values.getPropertiesDataType;

import java.text.MessageFormat;
import java.util.function.Predicate;

import com.dsl.classgen.annotations.InnerField;
import com.dsl.classgen.io.HashTableModel;

public final class InnerFieldGenerator implements GenerationOutputLog {
	
	private final String pattern1 = """
				@{0}(key = "{1}", hash = {2,number,#})
				\t\tpublic static final {3} {4} = {5};
				""";
	
	private final String pattern2 = """
				@{0}(key = "{1}", hash = {2})
				\t\tpublic static final {3} {4};
				""";
	
	public String generateInnerField(String fieldKey, String fieldValue, HashTableModel htm) {
		Integer hash = htm.hashTableMap.get(fieldKey);
		
		if(!fieldValue.isEmpty()) {
			formatGenerationOutput("Inner Field", fieldKey, null);
			return MessageFormat.format(pattern1,
					InnerField.class.getSimpleName(),
								fieldKey,
								hash,
								getPropertiesDataType(),
								parseFieldName(fieldKey),
								parseFieldValue(fieldValue));
		}
		formatGenerationOutput("Inner Field", fieldKey, "UNINITIALIZED FIELD");
		return MessageFormat.format(pattern2, 
				InnerField.class.getSimpleName(),
				fieldKey,
				hash,
				getPropertiesDataType(),
				parseFieldName(fieldKey));
	}

	private String parseFieldValue(String fieldValue) {
		Predicate<String> testValue = getPropertiesDataType()::equals;

		String pattern = testValue.test("String") ? "\"%s\"" : 
						 testValue.test("char") || testValue.test("Character") ? 
								 					"'%s'" : 
								 					"%s";
		return String.format(pattern, fieldValue);
	}
	
	private String parseFieldName(String data) {
		String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
		fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
		return fieldName.toUpperCase();
	}
}
