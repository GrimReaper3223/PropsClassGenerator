package com.dsl.classgen.io.sync_refs;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;

public final class SyncSource implements SyncOperations {

	private static final Logger LOGGER = LogManager.getLogger(SyncSource.class);
	
	@Override
	public boolean insertClassSection(CacheModel model) {
		return false;
	}


	@Override
	public boolean insertFieldSection(CacheModel model) {
		return false;
	}


	@Override
	public void eraseClassSection(CacheModel model) {
		String lookupPattern = ProcessAnnotation.processClassAnnotations(model.fileHash);
		
		if(lookupPattern != null) {
			String classSourceStartHint = lookupPattern.substring(0, lookupPattern.indexOf('@'));
			String classSourceEndHint = lookupPattern.substring(lookupPattern.indexOf('@') + 1);
			int endPatternFullIndex = classSourceEndHint.length();
			
			StringBuilder sb = Reader.readSource(FrameworkContext.get().getPathsContextInstance().getExistingPJavaGeneratedSourcePath());
			sb.delete(sb.indexOf(classSourceStartHint), sb.indexOf(classSourceEndHint) + endPatternFullIndex);
			
			Writer.write(sb.toString());
		} else {
			LOGGER.warn("Static inner class cannot be found.");
		}
	}


	@Override
	public boolean eraseFielSection(CacheModel model) {
		return false;
	}


	@Override
	public boolean modifyClassSection(CacheModel model) {
		return false;
	}


	@Override
	public boolean modifyFieldSection(CacheModel model) {
		return false;
	}
}
