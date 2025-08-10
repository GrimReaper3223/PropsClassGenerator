package com.dsl.classgen.models.model_mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The Class OutterClassModel.
 */
public class OutterClassModel {

	private static Map<String, InnerStaticClassModel> mapModel = new HashMap<>();
	
	private OutterClassModel() {}

	/**
	 * Compute class model to map.
	 *
	 * @param model the inner generated class model
	 */
	public static void computeClassModelToMap(InnerStaticClassModel model) {
		String stringFilePath = model.annotationMetadata().filePath().toString();

		if (mapModel.computeIfPresent(stringFilePath, (_, _) -> model) == null) {
			mapModel.put(stringFilePath, model);
		}
	}

	/**
	 * Gets the model.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param filePath the properties file path
	 * @return the mapped inner static class model
	 */
	public static <T> InnerStaticClassModel getModel(T filePath) {
		return mapModel.get(filePath.toString());
	}

	/**
	 * Check path in class model map.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param filePath the properties file path
	 * @return true, if exists in map model
	 */
	public static <T> boolean checkPathInClassModelMap(T filePath) {
		return mapModel.containsKey(filePath.toString());
	}

	/**
	 * Gets the map model stream.
	 *
	 * @return the map model values stream
	 */
	public static Stream<InnerStaticClassModel> getMapModelStream() {
		return mapModel.values().stream();
	}
}
