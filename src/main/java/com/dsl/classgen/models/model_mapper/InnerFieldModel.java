package com.dsl.classgen.models.model_mapper;

import java.lang.reflect.AccessFlag;

import com.dsl.classgen.models.Parsers;
import com.github.javaparser.ast.Modifier.Keyword;

public record InnerFieldModel (FieldAnnotationModel annotationMetadata, 
		Class<?> fieldType,
		String fieldName,
		Object rawFieldValue,
		Object parsedFieldValue,
		Keyword[] sourceModifiers,
		AccessFlag[] byteCodeModifiers) {
	
	public InnerFieldModel (FieldAnnotationModel annotationMetadata, Class<?> fieldType, String fieldName, Object rawFieldValue) {
		this(annotationMetadata, 
				fieldType,
				new Parsers() {}.parseFieldName(fieldName),
				rawFieldValue,
				new Parsers() {}.parseFieldValue(rawFieldValue.toString(), fieldType.getSimpleName()), 
				new Keyword[] { Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL },
				new AccessFlag[] { AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL });
	}
}
