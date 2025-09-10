package com.dsl.classgen.models.model_mapper;

import java.lang.reflect.AccessFlag;

import com.dsl.classgen.models.Parsers;
import com.github.javaparser.ast.Modifier.Keyword;

/**
 * The Record InnerFieldModel.
 *
 * @param annotationMetadata the annotation metadata
 * @param fieldType          the field type
 * @param fieldName          the field name
 * @param rawFieldValue      the raw field value
 * @param parsedFieldValue   the parsed field value
 * @param sourceModifiers    the source modifiers
 * @param byteCodeModifiers  the byte code modifiers
 */
public record InnerFieldModel(FieldAnnotationModel annotationMetadata, Class<?> fieldType, String fieldName,
		Object rawFieldValue, Object parsedFieldValue, Keyword[] sourceModifiers, AccessFlag[] byteCodeModifiers) {

	/**
	 * Instantiates a new inner field model.
	 *
	 * @param annotationMetadata the annotation metadata
	 * @param fieldType          the field type
	 * @param fieldName          the field name
	 * @param rawFieldValue      the raw field value
	 */
	public InnerFieldModel(FieldAnnotationModel annotationMetadata, Class<?> fieldType, String fieldName,
			Object rawFieldValue) {
		this(annotationMetadata, fieldType, new Parsers() {}.parseFieldName(fieldName), rawFieldValue,
				new Parsers() {}.parseFieldValue(rawFieldValue.toString(), fieldType.getSimpleName()),
				new Keyword[] { Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL },
				new AccessFlag[] { AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL });
	}

	@Override
	public final String toString() {
		return String.format("\t%s = %s : (%s)", fieldName, parsedFieldValue, fieldType.getSimpleName());
	}
}
