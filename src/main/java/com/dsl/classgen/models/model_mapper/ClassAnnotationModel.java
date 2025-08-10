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
public record ClassAnnotationModel(int hash, Path filePath, Class<?> javaType) implements Parsers {}
