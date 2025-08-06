package com.dsl.classgen.annotation.processors;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.models.CacheModel;

public class AnnotationProcessor {

	private AnnotationProcessor() {}
	
    public static List<Class<?>> processClassAnnotations(List<CacheModel> cacheModelList) {
    	List<Integer> hashList = cacheModelList.stream().map(model -> model.fileHash).toList();
    	
        return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
					  .filter(cl -> hashList.contains(cl.getDeclaredAnnotation(GeneratedInnerStaticClass.class).hash()))
					  .toList();
    }
    
    public static Field processFieldAnnotations(int fileHash, int fieldHash) {
    	return Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
		   			 .filter(cl -> Arrays.stream(cl.getDeclaredAnnotationsByType(GeneratedInnerStaticClass.class)).anyMatch(annon -> annon.hash() == fileHash))
		   			 .flatMap(cl -> Arrays.stream(cl.getDeclaredFields()))
		   			 .filter(field -> Arrays.stream(field.getDeclaredAnnotationsByType(GeneratedInnerField.class)).anyMatch(annon -> annon.hash() == fieldHash))
	    			 .findFirst()
	    			 .orElseThrow(() -> new IllegalArgumentException("No field found with the specified hash: " + fieldHash));
    }
}