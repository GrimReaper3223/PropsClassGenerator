package com.dsl.classgen.models.model_mapper;

import java.nio.file.Path;

import com.dsl.classgen.models.Parsers;

/**
 * The Record ClassAnnotationModel.
 *
 * @param hash     the file hash
 * @param filePath the file path
 * @param javaType the java type
 */
public record ClassAnnotationModel(int hash, Path filePath, Class<?> javaType) implements Parsers {

	@Override
	public final String toString() {
		return String.format("""
				\tHash: %d
				\tFile Path: %s
				\tJava Type: %s
				""", hash, filePath.toString(), javaType.getSimpleName());
	}
}
