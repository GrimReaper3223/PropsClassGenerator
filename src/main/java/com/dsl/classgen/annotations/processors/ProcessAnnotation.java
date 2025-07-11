package com.dsl.classgen.annotations.processors;

import java.util.Arrays;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.InnerField;
import com.dsl.classgen.io.file_manager.Reader;

public class ProcessAnnotation {
	
	private ProcessAnnotation() {}
	
    public static String processClassAnnotations(int fileHash) {
        return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		        	 .flatMap(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerClass.class)))
		        	 .filter(val -> val.hash() == fileHash)
		        	 .findFirst()
		        	 .map(val -> String.format("// CLASS HINT ~>> %s@// CLASS HINT <<~ %<s", val.filePath()))
		        	 .orElse(null);
    }
    
    public static String processFieldAnnotations(int fileHash, int fieldHash) {
    	return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
	    			 .filter(cl -> cl.getAnnotation(GeneratedInnerClass.class).hash() == fileHash)
	    			 .flatMap(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(InnerField.class)))
	    			 .filter(val -> val.hash() == fieldHash)
	    			 .findFirst()
	    			 .map(val -> String.format("// FIELD HINT ~>> %s@// FIELD HINT <<~ %<s", val.key()))
	    			 .orElse(null);
    }
}