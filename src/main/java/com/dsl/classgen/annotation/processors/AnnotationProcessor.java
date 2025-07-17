package com.dsl.classgen.annotation.processors;

import java.util.Arrays;

import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.utils.PatternType;
import com.dsl.classgen.utils.Utils;

public class AnnotationProcessor {
	
	private AnnotationProcessor() {}
	
    public static String processClassAnnotations(int fileHash) {
        return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		        	 .flatMap(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerStaticClass.class)))
		        	 .filter(val -> val.hash() == fileHash)
		        	 .findFirst()
		        	 .map(val -> Utils.formatSourcePattern(PatternType.CLASS,  val.filePath()))
		        	 .orElse(null);
    }
    
    public static String processFieldAnnotations(int fileHash, int fieldHash) { 
    	return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		   			 .filter(cl -> cl.getAnnotation(GeneratedInnerStaticClass.class).hash() == fileHash)
		   			 .flatMap(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerField.class)))
		   			 .filter(val -> val.hash() == fieldHash)
		   			 .findFirst()
	    			 .map(val -> Utils.formatSourcePattern(PatternType.FIELD, val.key()))
	    			 .orElse(null);
    }
}