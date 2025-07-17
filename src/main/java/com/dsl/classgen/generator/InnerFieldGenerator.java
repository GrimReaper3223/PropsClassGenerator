package com.dsl.classgen.generator;

import java.text.MessageFormat;
import java.util.Map;

import com.dsl.classgen.annotation.GeneratedInnerField;

public final class InnerFieldGenerator extends SupportProvider implements Parsers {
	
    private final String fieldPattern1 = """
    		// FIELD HINT ~>> {0}
    		\t\t{1}
    		\t\tpublic static final {2} {3} = {4};
        	\t// FIELD HINT <<~ {0}
    		""";
    
    private final String fieldPattern2 = """
        	// FIELD HINT ~>> {0}
    		\t\t{1}
    		\t\tpublic static final {2} {3};
    		\t// FIELD HINT <<~ {0}
    		""";
    
    public String generateInnerField(String fieldKey, String fieldValue, Integer hash) {
    	var map = Map.of("key", fieldKey, "hash", hash);
    	String propertiesDataType = pathsCtx.getPropertiesDataType();
    	
        if (!fieldValue.isEmpty()) {
            formatGenerationOutput("Inner Field", fieldKey, null);
            return MessageFormat.format(fieldPattern1,
            		propertiesDataType, 
            		createAnnotation(GeneratedInnerField.class, map),
            		propertiesDataType,
            		parseFieldName(fieldKey), 
            		parseFieldValue(fieldValue, propertiesDataType));
        }
        
        formatGenerationOutput("Inner Field", fieldKey, "UNINITIALIZED FIELD");
        return MessageFormat.format(fieldPattern2, 
        		createAnnotation(GeneratedInnerField.class, map),
        		propertiesDataType, 
        		parseFieldName(fieldKey));
    }
}

