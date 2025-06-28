package com.dsl.classgen.generators;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.io.cache_system.HashTableModel;
import com.dsl.classgen.utils.Utils;

public final class InnerStaticClassGenerator implements OutputLogGeneration {
	
    public String generateInnerStaticClass() {
        InnerFieldGenerator innerFieldGenerator = new InnerFieldGenerator();
        Path softPropertyFileName = Values.getSoftPropertiesFileName();
        String formattedClassName = formatClassName(softPropertyFileName);
        HashTableModel htm = Values.getElementFromHashTableMap(Utils.resolveJsonFilePath(softPropertyFileName));
        
        formatGenerationOutput("Static Inner Class", formattedClassName, null);
        
        return String.format("""
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
        		""", GeneratedInnerClass.class.getSimpleName(), 
        				Values.isSingleFile() ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().resolve(Values.getRawPropertiesfileName()), 
        				Values.getPropertiesDataType(),
        				htm.fileHash,
        				formattedClassName, 
        				PrivateConstructor.class.getSimpleName(), 
        				Values.getProps()
        					  .entrySet()
        					  .stream()
        					  .map(entry -> {
						          String key = entry.getKey().toString();
						          String value = entry.getValue().toString();
						          Integer hash = htm.hashTableMap.get(key);
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

