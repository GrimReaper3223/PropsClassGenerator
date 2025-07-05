package com.dsl.classgen.annotations.processors;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.io.file_manager.Reader;

import java.util.Arrays;

public class ProcessAnnotation {
	
	private ProcessAnnotation() {}
	
    public static void processAnnotations() {
        System.out.println('\n');
        
        Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
        	  .flatMap(cl -> {
		          System.out.println(cl.getAnnotation(GeneratedInnerClass.class));
		          return Arrays.stream(cl.getDeclaredFields())
		        		  	   .flatMap(f -> Arrays.stream(f.getDeclaredAnnotations()));
        	  }).forEach(System.out::println);
    }
}