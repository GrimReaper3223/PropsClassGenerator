package com.dsl.classgen.models;

import java.lang.reflect.AccessFlag;

import com.dsl.classgen.generator.ExtParsers;

public record InnerFieldModel (IFieldAnnotationModel annotationMetadata, 
		Class<?> fieldType,
		String fieldName,
		Object fieldValue,
		AccessFlag[] modifiers) implements Hints {
	
	public InnerFieldModel (IFieldAnnotationModel annotationMetadata, Class<?> fieldType, String fieldName, Object fieldValue) {
		this(annotationMetadata, 
				fieldType,
				ExtParsers.parseFieldNameHelper(fieldName),
				fieldType.cast(fieldValue), 
				new AccessFlag[] {
						AccessFlag.PUBLIC,
						AccessFlag.STATIC,
						AccessFlag.FINAL
				});
	}
	
	@Override
	public String startHint() {
		return String.format("// FIELD HINT ~>> %s", annotationMetadata.key());
	}

	@Override
	public String endHint() {
		return String.format("// FIELD HINT <<~ %s", annotationMetadata.key());
	}
}
