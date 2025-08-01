package com.dsl.classgen.models.model_mapper;

import java.util.Map;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.models.Parsers;

public record FieldAnnotationModel(String key, int hash) implements Parsers {
	
	public String getAnnotationString() {
		return createAnnotation(GeneratedInnerField.class, 
								Map.of("key", "\""+ key + "\"", 
															"hash", hash));
	}
}
