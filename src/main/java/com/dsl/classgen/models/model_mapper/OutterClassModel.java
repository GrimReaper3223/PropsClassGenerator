package com.dsl.classgen.models.model_mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class OutterClassModel {

	private static Map<String, InnerStaticClassModel> mapModel = new HashMap<>();

	private OutterClassModel() {}

	public static void computeModelToMap(InnerStaticClassModel model) {
		String stringFilePath = model.annotationMetadata().filePath().toString();

		if(mapModel.computeIfPresent(stringFilePath, (_, _) -> model) == null) {
			mapModel.put(stringFilePath, model);
		}
	}

	public static <T> InnerStaticClassModel getModel(T filePath) {
		return mapModel.get(filePath.toString());
	}

	public static <T> boolean isModelAlreadyLoaded(T filePath) {
		return mapModel.containsKey(filePath.toString());
	}

	public static Map<String, InnerStaticClassModel> getMapModel() {
		return mapModel;
	};

	public static Stream<InnerStaticClassModel> getMapModelStream() {
		return mapModel.values().stream();
	}
}
