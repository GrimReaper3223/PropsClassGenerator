package com.dsl.classgen.parsers;

import java.text.MessageFormat;
import java.util.function.Predicate;

import com.dsl.classgen.Generator;
import com.dsl.classgen.annotations.InnerField;

public final class InnerFieldParser implements Commons {
	
	private final String pattern1 = """
				@{0}
				\t\tpublic static final {1} {2} = {3};
				""";
	
	private final String pattern2 = """
				@{0}
				\t\tpublic static final {1} {2};
				""";
	public String parseInnerField(String fieldKey, String fieldValue) {
		if(!fieldValue.isEmpty()) {
			return MessageFormat.format(pattern1, 
					formatAnnotationClassName(InnerField.class),
								Generator.getPropertiesDataType(),
								formatData(fieldKey),
								formatFieldValuePattern(fieldValue));
		}
		return MessageFormat.format(pattern2, 
				formatAnnotationClassName(InnerField.class),
				Generator.getPropertiesDataType(),
				formatData(fieldKey));
	}

	private String formatFieldValuePattern(String fieldValue) {
		Predicate<String> testValue = Generator.getPropertiesDataType()::equals;

		String pattern = testValue.test("String") ? "\"%s\"" : 
						 testValue.test("char") || testValue.test("Character") ? 
								 					"'%s'" : 
								 					"%s";
		return String.format(pattern, fieldValue);
	}
	
	private String formatData(String data) {
		String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
		fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
		return fieldName.toUpperCase();
	}
}
