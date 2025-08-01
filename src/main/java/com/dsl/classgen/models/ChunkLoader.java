package com.dsl.classgen.models;

import org.apache.logging.log4j.LogManager;

import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;

public class ChunkLoader {

	private final GeneralContext generalCtx = GeneralContext.getInstance();
	private final PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	
	public void loadChunks() {
		LogManager.getLogger(ChunkLoader.class).info("Loading model hierarchy...");
		pathsCtx.getFileList()
				.forEach(path -> {
					OutterClassModel.computeClassModelToMap(InnerStaticClassModel.initInstance(path));
					pathsCtx.checkFileInCache(path);
				});
	}
}
