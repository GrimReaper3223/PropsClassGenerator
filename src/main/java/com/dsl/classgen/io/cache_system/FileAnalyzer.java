package com.dsl.classgen.io.cache_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.file_handler.Reader;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public interface FileAnalyzer {

	// retorna true se o cache ja existe e se e identico ao arquivo atualmente lido
	// false se uma das condicionais falharem
	static boolean hasValidCacheFile(Path path) {
		Reader.loadPropFile(path);
		Path jsonFilePath = Utils.resolveJsonFilePath(path);
		boolean result = false;
		
		if(Files.exists(jsonFilePath)) {
			try(BufferedReader br = Files.newBufferedReader(jsonFilePath)) {
				HashTableModel htm = new HashTableModel(path, Values.getProps());
				result = htm.equals(new Gson().fromJson(br, HashTableModel.class));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
