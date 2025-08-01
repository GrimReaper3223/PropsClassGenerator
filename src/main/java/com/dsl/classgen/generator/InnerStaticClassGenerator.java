package com.dsl.classgen.generator;

import java.nio.file.Path;
import java.util.stream.Collectors;

import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;

public final class InnerStaticClassGenerator extends SupportProvider {
	
    public String generateInnerStaticClass(InnerStaticClassModel model) {
    	InnerFieldGenerator fieldGenerator = new InnerFieldGenerator();
    	Path propertyPath = model.annotationMetadata().filePath();
    	
    	formatGenerationOutput("Static Inner Class", model.className(), null);
    	
        return String.format("""
        		%1$s
        		\t%2$s
        		\tpublic static final class %3$s {
        		
	        		\t@%4$s
	        		\tprivate %3$s() {}
	        		
	        		\t// PROPS-CONTENT-START: %5$s
	        		\t%6$s
	        		\t// PROPS-CONTENT-END: %5$s
        		\t}
        		\t%7$s
        		""",
				model.startHint(),
				model.annotationMetadata().getAnnotationString(),
        		model.className(), 
				GeneratedPrivateConstructor.class.getSimpleName(),
				propertyPath.getFileName(),
				model.fieldModelList()
					  .stream()
					  .map(fieldGenerator::generateInnerField)
					  .collect(Collectors.joining("\n\t\t")),
        		model.endHint());
    }
}

