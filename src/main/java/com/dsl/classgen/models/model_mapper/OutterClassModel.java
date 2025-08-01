package com.dsl.classgen.models.model_mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedOutterClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.models.Parsers;

public class OutterClassModel {
	
	private static Map<String, InnerStaticClassModel> mapModel = new HashMap<>();
	
	private OutterClassModel() {}
	
	public static String getImports() {
		return Parsers.createImports(List.of(
    			GeneratedOutterClass.class,
    			GeneratedInnerStaticClass.class,
    			GeneratedInnerField.class,
    			GeneratedPrivateConstructor.class));
	}
	
	public static void computeClassModelToMap(InnerStaticClassModel model) {
		String stringFilePath = model.annotationMetadata().filePath().toString();
		
		if(mapModel.computeIfPresent(stringFilePath, (_, _) -> model) == null) {
			mapModel.put(stringFilePath, model);
		}
	}
	
	public static <T> InnerStaticClassModel getModel(T filePath) {
		return mapModel.get(filePath.toString());
	}
	
	public static <T> boolean checkPathInClassModelMap(T filePath) {
		return mapModel.containsKey(filePath.toString());
	}
	
	public static Stream<InnerStaticClassModel> getMapModelStream() {
		return mapModel.values().stream();
	}
}
