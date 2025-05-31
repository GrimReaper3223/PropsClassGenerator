package com.dsl.classgen.parsers;

import com.dsl.classgen.Generator;
import com.dsl.classgen.annotations.InnerField;

public final class InnerFieldParser implements Commons {
	
	public String parseInnerField(String fieldKey, String fieldValue) {
		return String.format("""
				@%s
				\t\tpublic static final %s %s = "%s";
				""", formatAnnotationClassName(InnerField.class), Generator.getPropertiesDataType(), formatData(fieldKey), fieldValue);
	}

	private String formatData(String data) {
		String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
		fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
		return fieldName.toUpperCase();
	}
}
