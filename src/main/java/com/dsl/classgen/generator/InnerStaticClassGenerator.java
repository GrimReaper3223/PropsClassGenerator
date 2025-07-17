package com.dsl.classgen.generator;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;

public final class InnerStaticClassGenerator extends SupportProvider implements Parsers, Initializer {
	
	private String formattedClassName;
	private CacheModel cm;
	private Path inputPropertiesPath;
	private Map<String, ?> map;
	
	@Override
	public void initClass() {
		Path propertiesFileName = pathsCtx.getPropertiesFileName();
		
		inputPropertiesPath = flagsCtx.getIsSingleFile() ? pathsCtx.getInputPropertiesPath() : pathsCtx.getInputPropertiesPath().resolve(propertiesFileName);
		formattedClassName = parseClassName(propertiesFileName);
		cm = CacheManager.getElementFromCacheModelMap(Utils.resolveJsonFilePath(propertiesFileName));
		
		formatGenerationOutput("Static Inner Class", formattedClassName, null);
		
		map = Map.of("filePath", inputPropertiesPath.toString(), 
				 "javaType", pathsCtx.getPropertiesDataType() + ".class",
				 "hash", cm.fileHash);
	}
	
    public String generateInnerStaticClass() {
    	initClass();
    	
        return String.format("""
        		// CLASS HINT ~>> %1$s
        		\t%2$s
        		\tpublic static final class %3$s {
        		
	        		\t@%4$s
	        		\tprivate %3$s() {}
	        		
	        		\t// PROPS-CONTENT-START
	        		\t%5$s
	        		\t// PROPS-CONTENT-END
        		\t}
        		\t// CLASS HINT <<~ %1$s
        		""",
				inputPropertiesPath,
        		createAnnotation(GeneratedInnerStaticClass.class, map),
        		formattedClassName, 
				GeneratedPrivateConstructor.class.getSimpleName(),
				generalCtx.getProps()
					  .entrySet()
					  .stream()
					  .map(entry -> {
				          String key = entry.getKey().toString();
				          String value = entry.getValue().toString();
				          Integer hash = cm.hashTableMap.get(key);
				          return new InnerFieldGenerator().generateInnerField(key, value, hash);
					  }).collect(Collectors.joining("\n\t\t")));
    }
}

