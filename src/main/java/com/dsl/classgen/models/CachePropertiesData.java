package com.dsl.classgen.models;

import java.io.Serializable;

/**
 * The Record CachePropertiesData.
 *
 * @param propKey         the property key
 * @param rawPropValue    the raw property value
 * @param parsedPropValue the parsed property value
 */
public record CachePropertiesData(String propKey, Object rawPropValue, Object parsedPropValue) implements Serializable {

	@Override
	public final String toString() {
		return String.format("%s = %s", propKey, parsedPropValue.toString());
	}
}
