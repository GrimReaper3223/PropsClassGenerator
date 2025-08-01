package com.dsl.classgen.models.model_mapper;

import java.lang.reflect.AccessFlag;

import com.dsl.classgen.models.Hints;
import com.dsl.classgen.models.Parsers;

public record InnerFieldModel (FieldAnnotationModel annotationMetadata, 
		Class<?> fieldType,
		String fieldName,
		Object fieldValue,
		AccessFlag[] modifiers) implements Hints {
	
	public InnerFieldModel (FieldAnnotationModel annotationMetadata, Class<?> fieldType, String fieldName, Object fieldValue) {
		this(annotationMetadata, 
				fieldType,
				new Parsers() {}.parseFieldName(fieldName),
				new Parsers() {}.parseFieldValue(fieldValue.toString(), fieldType.toString()), 
				new AccessFlag[] {
						AccessFlag.PUBLIC,
						AccessFlag.STATIC,
						AccessFlag.FINAL
				});
	}
	
	@Override
	public String startHint() {
		return String.format("// INNER FIELD HINT ~>> %s", annotationMetadata.key());
	}

	@Override
	public String endHint() {
		return String.format("// INNER FIELD HINT <<~ %s", annotationMetadata.key());
	}
}
