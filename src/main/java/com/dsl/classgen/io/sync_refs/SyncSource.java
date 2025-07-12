package com.dsl.classgen.io.sync_refs;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.annotations.processors.ProcessAnnotation;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.io.file_manager.Writer;

public final class SyncSource implements SyncOperations {

	private static final Logger LOGGER = LogManager.getLogger(SyncSource.class);
	private final Supplier<StringBuilder> sbSupplier = () -> Reader.readSource(pathsCtx.getExistingPJavaGeneratedSourcePath()); 
	
	@Override
	public void insertClassSection(Path path) {
		final String propsFileEndPattern = "// PROPS-FILE-END";
		StringBuilder sb = sbSupplier.get();
		int propsFileEndIndex = sb.indexOf(propsFileEndPattern) - 1;	// -1 retorna para a linha acima, evitando sobrescrever o padrao no arquivo
		
		Reader.read(path);
		pathsCtx.addFileToCacheList(path); 	// adiciona o arquivo a lista para que o cache dele seja criado
		pathsCtx.setInputPropertiesPath(path);
		CacheManager.processCache();
		String generatedClass = '\t' + innerClassGen.generateInnerStaticClass() + '\n';

		sb.insert(propsFileEndIndex, generatedClass);
		Writer.write(sb.toString());
	}

	@Override
	public void eraseClassSection(CacheModel model) {
		String lookupPattern = ProcessAnnotation.processClassAnnotations(model.fileHash);
		
		if(lookupPattern != null) {
			String classSourceStartHint = lookupPattern.substring(0, lookupPattern.indexOf('@'));
			String classSourceEndHint = lookupPattern.substring(lookupPattern.indexOf('@') + 1);
			int endPatternFullIndex = classSourceEndHint.length();
			
			StringBuilder sb = sbSupplier.get();
			sb.delete(sb.indexOf(classSourceStartHint) - 1, sb.indexOf(classSourceEndHint) + endPatternFullIndex + 2);
			
			Writer.write(sb.toString());
		} else {
			LOGGER.warn("Static inner class cannot be found.");
		}
	}

	@Override
	public void modifyFieldSection(CacheModel model) {
	}
}
