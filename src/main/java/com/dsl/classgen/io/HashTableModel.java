package com.dsl.classgen.io;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HashTableModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String filePath;
	public int fileHash;
	
	public Map<String, Integer> hashTableMap;
	
	public HashTableModel(Path filePath) {
		this.filePath = filePath.toString();
		fileHash = hashCode();
	}
	
	public void initPropertyMapGraph() {
		hashTableMap = Values.getProps().entrySet()
						 .stream()
						 .map(entry -> Map.entry(entry.getKey().toString(), Objects.hash(entry.getKey().toString(), entry.getValue())))
						 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	@Override
	public boolean equals(Object obj) {
		HashTableModel htm = (HashTableModel) obj;
		boolean result = htm.hashTableMap.entrySet().containsAll(this.hashTableMap.entrySet()); 
		return htm.fileHash == this.fileHash && result;
	}
	
	@Override
	public int hashCode() {
		BasicFileAttributes attrs = null;
		
		try {
			attrs = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileTime creationTime = attrs.creationTime();
		FileTime lastModifiedTime = attrs.lastModifiedTime();
		long fileSize = attrs.size();
		
		return Objects.hash(creationTime.toMillis(), lastModifiedTime.toMillis(), fileSize);
	}
}
