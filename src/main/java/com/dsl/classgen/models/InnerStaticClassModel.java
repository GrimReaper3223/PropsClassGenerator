package com.dsl.classgen.models;

import java.lang.reflect.AccessFlag;

import com.dsl.classgen.generator.ExtParsers;

public record InnerStaticClassModel (IClassAnnotationModel annotationMetadata, String fileName, AccessFlag[] modifiers) implements Hints {
	
	public InnerStaticClassModel (IClassAnnotationModel annotationMetadata, String fileName) {
		this(annotationMetadata, 
				ExtParsers.parseClassNameHelper(fileName, null),
				new AccessFlag[] {
				AccessFlag.PUBLIC,
				AccessFlag.STATIC,
				AccessFlag.FINAL
		});
	}
	
	@Override
	public String startHint() {
		return String.format("// CLASS HINT ~>> %s", annotationMetadata.filePath());
	}

	@Override
	public String endHint() {
		return String.format("// CLASS HINT <<~ %s", annotationMetadata.filePath());
	}
}
