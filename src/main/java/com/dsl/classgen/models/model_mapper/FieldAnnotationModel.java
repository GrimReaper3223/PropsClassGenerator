package com.dsl.classgen.models.model_mapper;

import com.dsl.classgen.models.Parsers;

/**
 * The Record FieldAnnotationModel.
 *
 * @param key  the property key
 * @param hash the key and value property hash
 */
public record FieldAnnotationModel(String key, int hash) implements Parsers {}
