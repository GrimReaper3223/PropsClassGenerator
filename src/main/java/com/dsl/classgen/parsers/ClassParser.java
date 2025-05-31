package com.dsl.classgen.parsers;

import java.util.stream.Collectors;

import com.dsl.classgen.Generator;
import com.dsl.classgen.annotations.GeneratedClass;
import com.dsl.classgen.annotations.PrivateConstructor;

public final class ClassParser implements Commons {
	
	public String parseClass() {
		InnerStaticClassParser parser = new InnerStaticClassParser();
		
		return String.format("""
				package %1$s.generated;
				
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
				""", Generator.getPackageOfGeneratedClass(),
				formatAnnotationClassName(GeneratedClass.class),
				Generator.getOutterClassName(),
				formatAnnotationClassName(PrivateConstructor.class),
				Generator.isSingleFile() ? parser.parseInnerStaticClass() : 
										 Generator.getPathList()
										 		  .stream()
										 		  .map(path -> {
										 			 Generator.loadPropFile(path);
										 			 return parser.parseInnerStaticClass();
										 		  })
										 		  .collect(Collectors.joining("\n\t")));
	}
}
