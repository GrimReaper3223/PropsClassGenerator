package com.dsl.classgen.generator;

import java.util.stream.Collectors;

import com.dsl.classgen.annotation.GeneratedOutterClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;

public final class OutterClassGenerator extends SupportProvider {
	
	private final InnerStaticClassGenerator innerStaticClassGenerator = new InnerStaticClassGenerator();
	private String outterClassName = pathsCtx.getOutterClassName();
	
    public void generateOutterClass() {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating classes...\n");
    	formatGenerationOutput("Outter Class", outterClassName, "\n");
    	
        pathsCtx.setGeneratedClass(String.format("""
                package %1$s;
                
                %2$s
                
                /*
                 *	*** DO NOT DELETE THE COMMENT BELOW! ***
                 *
                 * 	If you need to add temporary comments related to static
                 *	inner fields or classes, add them between the delimiters,
                 *	as they will be erased when a given section is regenerated.
                 */ 
                @%3$s
                public final class %4$s {
                
                	@%5$s
                	private %4$s() {}
                	
                	// CLASS-FILE-START
                	%6$s
                	// CLASS-FILE-END
                }
                """, 
                pathsCtx.getPackageClass(),
                OutterClassModel.getImports(),
	        	GeneratedOutterClass.class.getSimpleName(),
	        	outterClassName, 
	        	GeneratedPrivateConstructor.class.getSimpleName(), 
        		OutterClassModel.getMapModelStream()
			        			.map(innerStaticClassGenerator::generateInnerStaticClass)
			        			.collect(Collectors.joining("\n\t"))));
    }
}