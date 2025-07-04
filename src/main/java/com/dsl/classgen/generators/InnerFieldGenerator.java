package com.dsl.classgen.generators;

import com.dsl.classgen.annotations.InnerField;
import java.text.MessageFormat;
import java.util.function.Predicate;

public final class InnerFieldGenerator implements OutputLogGeneration {
	
    private final String fieldPattern1 = """
    		// FIELD HINT ~>> {1}
    		\t\t@{0}(key = \"{1}\", hash = {2,number,#})
    		\t\tpublic static final {3} {4} = {5};
        	\t// FIELD HINT <<~ {1}
    		""";
    
    private final String fieldPattern2 = """
        	// FIELD HINT ~>> {1}
    		\t\t@{0}(key = \"{1}\", hash = {2,number,#})
    		\t\tpublic static final {3} {4};
    		// FIELD HINT <<~ {1}
    		""";

    public String generateInnerField(String fieldKey, String fieldValue, Integer hash) {
        if (!fieldValue.isEmpty()) {
            formatGenerationOutput("Inner Field", fieldKey, null);
            return MessageFormat.format(fieldPattern1, 
            		InnerField.class.getSimpleName(), 
            		fieldKey, 
            		hash, 
            		OutterClassGenerator.pathsCtx.getPropertiesDataType(), 
            		parseFieldName(fieldKey), 
            		parseFieldValue(fieldValue));
        }
        
        formatGenerationOutput("Inner Field", fieldKey, "UNINITIALIZED FIELD");
        return MessageFormat.format(fieldPattern2, 
        		InnerField.class.getSimpleName(), 
        		fieldKey, 
        		hash, 
        		OutterClassGenerator.pathsCtx.getPropertiesDataType(), 
        		parseFieldName(fieldKey));
    }

    private String parseFieldValue(String fieldValue) {
        Predicate<String> testValue = OutterClassGenerator.pathsCtx.getPropertiesDataType()::equals;
        String pattern = testValue.test("String") ? "\"%s\"" : 
        				  testValue.test("char") || testValue.test("Character") ? "'%s'" : "%s";
        return String.format(pattern, fieldValue);
    }

    private String parseFieldName(String data) {
        String fieldName = data.replaceAll("[.-]+", "_").toUpperCase();
        fieldName = fieldName.replaceAll("[\\s\\Q`!@#$%^&*()+{}:\"<>?|\\~/.;',[]=-\\E]+", "");
        return fieldName.toUpperCase();
    }
}

