package com.dsl.classgen.models;

/**
 * The Record CachePropertiesData.
 *
 * @param propKey         the property key
 * @param rawPropValue    the raw property value
 * @param parsedPropValue the parsed property value
 */
public record CachePropertiesData(String propKey, Object rawPropValue, Object parsedPropValue) {}
