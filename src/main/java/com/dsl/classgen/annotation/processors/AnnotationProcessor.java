package com.dsl.classgen.annotation.processors;

import java.util.Arrays;
import java.util.List;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.models.CacheModel;

public class AnnotationProcessor {

	private AnnotationProcessor() {}
	
    public static List<String> processClassAnnotations(List<CacheModel> cacheModelList) {
    	List<Integer> hashList = cacheModelList.stream().map(model -> model.fileHash).toList();
    	
        return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		        	 .flatMap(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerStaticClass.class)))
		        	 .filter(val -> hashList.contains(val.hash()))
		        	 .map(val -> String.format("// INNER CLASS HINT ~>> %s@// INNER CLASS HINT <<~ %<s", val.filePath()))
		        	 .toList();
    }
    
    public static String processFieldAnnotations(int fileHash, int fieldHash) {
    	return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		   			 .filter(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerStaticClass.class)).anyMatch(annon -> annon.hash() == fileHash))
		   			 .flatMap(cl -> Arrays.stream(cl.getDeclaredFields()))
		   			 .flatMap(field -> Arrays.stream(field.getDeclaredAnnotationsByType(GeneratedInnerField.class)))
		   			 .filter(annon -> annon.hash() == fieldHash)
		   			 .findFirst()
	    			 .map(annon -> String.format("// INNER FIELD HINT ~>> %s@// INNER FIELD HINT <<~ %<s", annon.key()))
	    			 .orElseThrow();
    }
}