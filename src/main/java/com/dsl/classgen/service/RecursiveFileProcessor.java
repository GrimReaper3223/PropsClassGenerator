package com.dsl.classgen.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.utils.LogLevels;
import com.dsl.classgen.utils.Utils;

public class RecursiveFileProcessor extends RecursiveAction {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LogManager.getLogger(RecursiveFileProcessor.class);

	private static GeneralContext generalCtx = GeneralContext.getInstance();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	private final Entry<Kind<Path>, Path> entry;

	public RecursiveFileProcessor(Entry<Kind<Path>, Path> entry) {
		this.entry = entry;
	}

	@Override
	protected void compute() {
		var key = entry.getKey();
		var value = entry.getValue();
		LOGGER.log(LogLevels.NOTICE.getLevel(), "{}: {}", key.name(), value);

	 	if(Files.isDirectory(value)) {
	 		pathsCtx.queueDir(value);

	 		try(Stream<Path> files = Files.list(value)) {
	 			files.forEach(path -> {
	 				var subEntry = Map.entry(key, path);

	 				if(Files.isDirectory(path)) {
	 					invokeAll(new RecursiveFileProcessor(subEntry));
	 				} else if(Utils.isPropertiesFile(path)){
	 					pathsCtx.queueChangedFileEntry(subEntry);
	 				}
	 			});
	 		} catch (IOException e) {
				Utils.handleException(e);
			}
	 	} else {
	 		pathsCtx.queueChangedFileEntry(entry);
	 	}
	}
}
