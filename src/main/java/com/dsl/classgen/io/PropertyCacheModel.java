package com.dsl.classgen.io;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Objects;
import java.util.stream.Collectors;

public class PropertyCacheModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public transient Path propertyFilePath;
	public String propertyFileStringPath;
	public int cachedPropertyFileHash;
	
	public PropertyCacheModel(Path propertyFilePath) {
		this.propertyFilePath = propertyFilePath;
		this.propertyFileStringPath = propertyFilePath.toString();
		this.cachedPropertyFileHash = hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		PropertyCacheModel instance = (PropertyCacheModel) obj;
		return instance.cachedPropertyFileHash == this.cachedPropertyFileHash;
	}
	
	@Override
	public int hashCode() {
		PosixFileAttributes attrs = null;
		
		try {
			attrs = Files.readAttributes(propertyFilePath, PosixFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileTime creationTime = attrs.creationTime();
		FileTime lastModifiedTime = attrs.lastModifiedTime();
		int permissions = Integer.valueOf(attrs.permissions().stream().map(p -> String.valueOf(p.ordinal())).collect(Collectors.joining()));
		long fileSize = attrs.size();
		
		return Objects.hash(creationTime.toMillis(), lastModifiedTime.toMillis(), permissions, fileSize);
	}
	
	public Path getPropertyFilePath() {
		return propertyFilePath;
	}
	
	public int getCachedPropertyFileHash() {
		return cachedPropertyFileHash;
	}
}
