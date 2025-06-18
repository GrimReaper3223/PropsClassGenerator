package com.dsl.classgen.generators;

import java.util.stream.Collectors;

import com.dsl.classgen.annotations.GeneratedClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.Reader;
import com.dsl.classgen.io.Values;

public final class OutterClassGenerator implements GenerationOutputLog {
	
	public void generateOutterClass() {
		InnerStaticClassGenerator innerStaticClassGenerator = new InnerStaticClassGenerator();
		String outterClassName = Values.getOutterClassName();
		
		System.out.println("\nGenerating classes...");
		formatGenerationOutput("Outter Class", outterClassName, null);
		
		Values.setGeneratedClass(String.format("""
				package %1$s;
				
				import com.dsl.classgen.annotations.GeneratedClass;
				import com.dsl.classgen.annotations.GeneratedInnerClass;
				import com.dsl.classgen.annotations.InnerField;
				import com.dsl.classgen.annotations.PrivateConstructor;
				
				@%2$s
				public final class %3$s {
					
					@%4$s
					private %3$s() {}
					
					%5$s
				}
				""", Values.getPackageClass(),
				GeneratedClass.class.getSimpleName(),
				outterClassName,
				PrivateConstructor.class.getSimpleName(),
				Values.isSingleFile() ? innerStaticClassGenerator.generateInnerStaticClass() : 
										 Values.getFileList()
									 		   .stream()
									 		   .map(path -> {
									 			  Reader.loadPropFile(path);
									 			  return innerStaticClassGenerator.generateInnerStaticClass();
									 		   })
									 		   .collect(Collectors.joining("\n\t"))));
	}
}
