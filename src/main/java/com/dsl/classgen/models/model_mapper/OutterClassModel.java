package com.dsl.classgen.models.model_mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class OutterClassModel {

	private static Map<String, InnerStaticClassModel> mapModel = new HashMap<>();

	private OutterClassModel() {}

	public static void computeModelToMap(InnerStaticClassModel model) {
		String stringFilePath = model.annotationMetadata().filePath().toString();
		mapModel.compute(stringFilePath, (_, value) -> value != null ? model.equals(value) ? value : model : model);
	}

	public static <T> InnerStaticClassModel getModel(T filePath) {
		return mapModel.get(filePath.toString());
	}

	public static <T> boolean isModelValid(T filePath) {
		boolean result = false;
		if(mapModel.containsKey(filePath.toString())) {
			result = getModel(filePath).equalsFileHashCode(filePath);
		}
		return result;
	}

	public static Map<String, InnerStaticClassModel> getMapModel() {
		return mapModel;
	}

	public static Stream<InnerStaticClassModel> getMapModelStream() {
		return mapModel.values().stream();
	}

	public static <T> void removeModelFromMap(T path) {
		mapModel.remove(path.toString());
	}
}
