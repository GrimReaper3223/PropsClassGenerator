package com.dsl.classgen.parsers;

import java.util.stream.Collectors;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.Values;

public final class InnerStaticClassParser implements OutputFormatter {

	public String parseInnerStaticClass() {
		InnerFieldParser innerParser = new InnerFieldParser();
		String formattedClassName = formatClassName(Values.getPropertiesFileName());
		
		formatGenerationOutput("Static Inner Class", formattedClassName, null);
		
		return String.format("""
				@%1$s(filePath = "%2$s.properties", javaType = %3$s.class)
				\tpublic static final class %4$s {
					
					\t@%5$s
					\tprivate %4$s() {}
					
					\t%6$s
				\t}
				""", GeneratedInnerClass.class.getSimpleName(),
				Values.getInputPath().resolve(Values.getPropertiesFileName()),
				Values.getPropertiesDataType(),
				formattedClassName,
				PrivateConstructor.class.getSimpleName(),
				Values.getProps()
					  .entrySet()
					  .stream()
					  .map(entry -> innerParser.parseInnerField(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())))
					  .collect(Collectors.joining("\n\t\t")));
	}
	
	private String formatClassName(String className) {
		String fileName = className.replaceAll("[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
		String fileNameUpperCaseFirstLetter = Character.toString(Character.toUpperCase(fileName.charAt(0)));
		return fileName.replaceFirst(String.valueOf(fileName.charAt(0)), fileNameUpperCaseFirstLetter);
	}
}
