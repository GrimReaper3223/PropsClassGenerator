package com.dsl.classgen.parsers;

import java.util.stream.Collectors;

import com.dsl.classgen.Generator;
import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;

public final class InnerStaticClassParser implements Commons {

	public String parseInnerStaticClass() {
		InnerFieldParser innerParser = new InnerFieldParser();
		
		return String.format("""
				@%1$s
				\tpublic static final class %2$s {
					
					\t@%3$s
					\tprivate %2$s() {}
					
					\t%4$s
				\t}
				""", formatAnnotationClassName(GeneratedInnerClass.class), 
				formatClassName(Generator.getPropertiesFileName()),
				formatAnnotationClassName(PrivateConstructor.class),
				Generator.getPropertyObject()
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
