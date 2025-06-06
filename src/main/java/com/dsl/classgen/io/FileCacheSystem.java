package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public final class FileCacheSystem {
	
	private static void createCacheDir() throws IOException {
		if(Files.notExists(Values.getCacheDirs())) {
			Files.createDirectories(Values.getCacheDirs());
		}
	}
	
	public static void processCache() {
		try {
			createCacheDir();
			Writer.writeJson();
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}
}
