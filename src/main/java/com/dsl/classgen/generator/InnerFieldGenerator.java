package com.dsl.classgen.generator;

import java.text.MessageFormat;

import com.dsl.classgen.models.model_mapper.InnerFieldModel;

public final class InnerFieldGenerator extends SupportProvider {
	
    private final String fieldPattern1 = """
    		{0}
    		\t\t{1}
    		\t\tpublic static final {2} {3} = {4};
        	\t{5}
    		""";
    
    public String generateInnerField(InnerFieldModel fieldModel) {
        formatGenerationOutput("Inner Field", fieldModel.annotationMetadata().key(), null);
        return MessageFormat.format(fieldPattern1,
        		fieldModel.startHint(), 
        		fieldModel.annotationMetadata().getAnnotationString(),
        		fieldModel.fieldType().getSimpleName(),
        		fieldModel.fieldName(), 
        		fieldModel.fieldValue(),
        		fieldModel.endHint());
    }
}

