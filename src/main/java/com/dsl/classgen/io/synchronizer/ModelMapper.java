package com.dsl.classgen.io.synchronizer;

import static com.dsl.classgen.io.synchronizer.SyncOptions.DELETE;
import static com.dsl.classgen.io.synchronizer.SyncOptions.INSERT;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelMapper <T extends Map<String, Integer>> {

	final Map<SyncOptions, Map<String, Integer>> modelMap;
	
	public ModelMapper(T oldMap, T newMap) {
		modelMap = mapper(oldMap, newMap);
	}
	
	// inicia o fluxo de processamento de um mapa
	private final BiFunction<T, T, Stream<Map.Entry<String, Integer>>> streamMapCreator = 
							 			(map1, map2) -> map1.entrySet()
							 							    .stream()
															.filter(entry -> !map2.containsValue(entry.getValue()));
							 			
	// finaliza o fluxo de processamento e mapa retornando um resultado
	private final BiFunction<Stream<Map.Entry<String, Integer>>, 
    								SyncOptions,
    								Map<SyncOptions, Map<String, Integer>>> streamMapFinisher = 
    									(stream, op) -> stream.flatMap(entry -> Map.of(op, entry)
    																			   .entrySet()
    																			   .stream())
    														  .collect(Collectors.groupingBy(Map.Entry::getKey,
    																  	Collectors.toMap(entry -> entry.getValue().getKey(), 
    																  					 entry -> entry.getValue().getValue())));
	
	public Map<SyncOptions, Map<String, Integer>> mapper(T oldMap, T newMap) {
		Map<SyncOptions, Map<String, Integer>> map = new EnumMap<>(SyncOptions.class);

		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(oldMap, newMap), DELETE));
		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(newMap, oldMap), INSERT));
		
		return map;
	}
}
