package com.dsl.classgen.generator;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public non-sealed interface ExtParsers extends Parsers {
	
	// sobrescreve o contrato dos metodos para controlar o comportamento que pode ou nao ser acessado caso esta interface seja implementada
	@Override
	default <T> String createAnnotation(Class<? extends Annotation> annotationClass, Map<String, T> elementMap) {
		return null;
	}
	
	@Override
	default String createImports(List<Class<? extends Annotation>> annotationClassList) {
		return null;
	}
	
	static String parseFieldNameHelper(String data) {
		return new ExtParsers() {}.parseFieldName(data);
	}
	
	@Override
	default String parseFieldValue(String fieldValue, String propertiesDataType) {
		return null;
	}
	
	static <T> String parseClassNameHelper(T propertiesFileName, String appendFileExt) {
		String parsedResult = new ExtParsers() {}.parseClassName(Path.of(String.valueOf(propertiesFileName)));
		return appendFileExt != null ? parsedResult.concat(appendFileExt) : parsedResult;
	}
}
