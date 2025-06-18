package com.dsl.classgen.generators;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.Values;

public final class InnerStaticClassGenerator implements GenerationOutputLog {

	public String generateInnerStaticClass() {
		InnerFieldGenerator innerFieldGenerator = new InnerFieldGenerator();
		String formattedClassName = formatClassName(Values.getPropertiesFileName());
		
		formatGenerationOutput("Static Inner Class", formattedClassName, null);
		
		return String.format("""
				@%1$s(filePath = "%2$s", javaType = %3$s.class)
				\tpublic static final class %4$s {
					
					\t@%5$s
					\tprivate %4$s() {}
					
					\t%6$s
				\t}
				""", GeneratedInnerClass.class.getSimpleName(),
				Values.isSingleFile() ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().resolve(Values.getPropertiesFileName()),
				Values.getPropertiesDataType(),
				
				formattedClassName,
				PrivateConstructor.class.getSimpleName(),
				Values.getProps()
					  .entrySet()
					  .stream()
					  .map(entry -> {
						  String key = entry.getKey().toString();
						  String value = entry.getValue().toString();
						  
						  return innerFieldGenerator.generateInnerField(key, value, Values.getElementFromHashTableMap(key));
					  })
					  .collect(Collectors.joining("\n\t\t")));
	}
	
	private String formatClassName(String className) {
		final String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
		return Arrays.stream(className.split(regex))
			  .map(token -> token.replaceFirst(Character.toString(token.charAt(0)), Character.toString(Character.toUpperCase(token.charAt(0)))))
			  .collect(Collectors.joining());
	}
}
