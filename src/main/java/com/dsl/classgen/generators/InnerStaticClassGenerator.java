package com.dsl.classgen.generators;

import static com.dsl.classgen.generators.OutterClassGenerator.fwCtx;
import static com.dsl.classgen.generators.OutterClassGenerator.pathsCtx;
import static com.dsl.classgen.generators.OutterClassGenerator.flagsCtx;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;

public final class InnerStaticClassGenerator implements OutputLogGeneration {
	
    public String generateInnerStaticClass() {
        InnerFieldGenerator innerFieldGenerator = new InnerFieldGenerator();
        Path softPropertyFileName = pathsCtx.getSoftPropertiesFileName();
        String formattedClassName = formatClassName(softPropertyFileName);
        CacheModel cm = CacheManager.getElementFromCacheModelMap(Utils.resolveJsonFilePath(softPropertyFileName));
        
        formatGenerationOutput("Static Inner Class", formattedClassName, null);
        
        String innerClassPattern ="""
        		// CLASS HINT ~>> %2$s
        		\t@%1$s(filePath = \"%2$s\", javaType = %3$s.class, hash = %4$d)
        		\tpublic static final class %5$s {
        		
	        		\t@%6$s
	        		\tprivate %5$s() {}
	        		
	        		\t// PROPS-CONTENT-START
	        		\t%7$s
	        		\t// PROPS-CONTENT-END
        		\t}
        		\t// CLASS HINT <<~ %2$s
        		""";
        
        return String.format(innerClassPattern, GeneratedInnerClass.class.getSimpleName(), 
        				flagsCtx.getIsSingleFile() ? pathsCtx.getInputPropertiesPath() : pathsCtx.getInputPropertiesPath().resolve(pathsCtx.getPropertiesFileName()), 
        				pathsCtx.getPropertiesDataType(),
        				cm.fileHash,
        				formattedClassName, 
        				PrivateConstructor.class.getSimpleName(), 
        				fwCtx.getProps()
        					  .entrySet()
        					  .stream()
        					  .map(entry -> {
						          String key = entry.getKey().toString();
						          String value = entry.getValue().toString();
						          Integer hash = cm.hashTableMap.get(key);
						          return innerFieldGenerator.generateInnerField(key, value, hash);
        					  }).collect(Collectors.joining("\n\t\t")));
    }

    private String formatClassName(Path softPropertyFileName) {
        String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
        return Arrays.stream(softPropertyFileName.toString().split(regex))
        			 .map(token -> token.replaceFirst(Character.toString(token.charAt(0)), Character.toString(Character.toUpperCase(token.charAt(0)))))
        			 .collect(Collectors.joining());
    }
}

