package com.dsl.classgen.io.synchronizer;

import static com.dsl.classgen.io.synchronizer.SyncOptions.DELETE;
import static com.dsl.classgen.io.synchronizer.SyncOptions.INSERT;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsl.classgen.models.CachePropertiesData;

public class ModelMapper <T extends Map<Integer, CachePropertiesData>> {

	// inicia o fluxo de processamento de um mapa
	private final BiFunction<T, T, Stream<Map.Entry<Integer, CachePropertiesData>>> streamMapCreator = 
							 			(map1, map2) -> map1.entrySet()
							 							    .stream()
															.filter(entry -> !map2.containsKey(entry.getKey()));
							 			
	// finaliza o fluxo de processamento e mapa retornando um resultado
	private final BiFunction<Stream<Map.Entry<Integer, CachePropertiesData>>, 
    								SyncOptions,
    								Map<SyncOptions, Map<Integer, CachePropertiesData>>> streamMapFinisher = 
    									(stream, op) -> stream.flatMap(entry -> Map.of(op, entry)
    																			   .entrySet()
    																			   .stream())
    														  .collect(Collectors.groupingBy(Map.Entry::getKey,
    																  	Collectors.toMap(entry -> entry.getValue().getKey(), 
    																  					 entry -> entry.getValue().getValue())));
	
	public Map<SyncOptions, Map<Integer, CachePropertiesData>> mapper(T oldMap, T newMap) {
		Map<SyncOptions, Map<Integer, CachePropertiesData>> map = new EnumMap<>(SyncOptions.class);

		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(oldMap, newMap), DELETE));
		map.putAll(streamMapFinisher.apply(streamMapCreator.apply(newMap, oldMap), INSERT));
		
		return map;
	}
}
