package com.dsl.classgen.models.model_mapper;

import java.nio.file.Path;
import java.util.Map;

import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.models.Parsers;

public record ClassAnnotationModel(int hash, Path filePath, Class<?> javaType) implements Parsers {
	
	public String getAnnotationString() {
		return createAnnotation(GeneratedInnerStaticClass.class, 
    			Map.of("hash", hash(),
    					"javaType", javaType().getSimpleName().concat(".class"),
    					"filePath", "\"" + filePath().toString() + "\""));
	}
}
