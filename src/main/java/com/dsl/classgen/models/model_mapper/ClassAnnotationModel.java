package com.dsl.classgen.models.model_mapper;

import java.nio.file.Path;

import com.dsl.classgen.models.Parsers;

public record ClassAnnotationModel(int hash, Path filePath, Class<?> javaType) implements Parsers {}
