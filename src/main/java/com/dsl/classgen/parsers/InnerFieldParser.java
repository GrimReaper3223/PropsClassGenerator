package com.dsl.classgen.parsers;

import java.text.MessageFormat;
import java.util.function.Predicate;

import static com.dsl.classgen.io.Values.getPropertiesDataType;

import com.dsl.classgen.annotations.InnerField;

public final class InnerFieldParser implements FormatUtils {
	
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
			formatConsoleOutput("Inner Field", fieldKey, null);
			return MessageFormat.format(pattern1, 
					formatAnnotationClassName(InnerField.class),
								getPropertiesDataType(),
								formatData(fieldKey),
								formatFieldValuePattern(fieldValue));
		}
		formatConsoleOutput("Inner Field", fieldKey, "UNINITIALIZED FIELD");
		return MessageFormat.format(pattern2, 
				formatAnnotationClassName(InnerField.class),
				getPropertiesDataType(),
				formatData(fieldKey));
	}

	private String formatFieldValuePattern(String fieldValue) {
		Predicate<String> testValue = getPropertiesDataType()::equals;

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
