package com.dsl.classgen.io.synchronizer;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.nio.file.Path;
import java.util.List;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.utils.Utils;

@SuppressWarnings("preview")
public final class SyncBin extends SupportProvider implements SyncOperations {

	private static final ClassFile cf = ClassFile.of();
	private ClassModel cm;
	
	public SyncBin() {
		try {
			cm = cf.parse(pathsCtx.getOutputClassFilePath());
		} catch (IOException e) {
			LOGGER.catching(e);
		}
	}

	@Override
	public void insertClassSection(Path path) {
		// document why this method is empty
	}

	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		List<Path> fileNameList = currentCacheModelList.stream()
													   .map(model -> {
														   Path convertedPath = null;  
														   try {
															   convertedPath = Utils.convertSourcePathToClassPath(model.filePath);
														   } catch (ClassNotFoundException e) {
															   LOGGER.catching(e);
														   }
														   return convertedPath;
													   })
													   .toList();
		
		byte[] newBytes = cf.build(cm.thisClass().asSymbol(), classBuilder -> 
			cm.attributes()
				.stream()
				.filter(InnerClassesAttribute.class::isInstance)
				.map(InnerClassesAttribute.class::cast)
				.flatMap(attr -> attr.classes().stream())
				.filter(elem -> !fileNameList.contains(Path.of(elem.innerClass().name().stringValue())))
				.forEach(elem -> classBuilder.withSuperclass(elem.innerClass().asSymbol())));
		
		Writer.write(newBytes);
	}
	
	@Override
	public void modifySection(CacheModel model) {
		// document why this method is empty
	}
}
