package com.dsl.classgen.generators;

import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;

import com.dsl.classgen.annotations.GeneratedClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.file_manager.Reader;

public final class OutterClassGenerator implements OutputLogGeneration {
	
    public void generateOutterClass() {
        InnerStaticClassGenerator innerStaticClassGenerator = new InnerStaticClassGenerator();
        String outterClassName = pathsCtx.getOutterClassName();
        logger.log(Level.INFO, "Generating classes...\n");
        formatGenerationOutput("Outter Class", outterClassName, "\n");
        
        String outterClassPattern = """
                package %1$s;
                
                import com.dsl.classgen.annotations.GeneratedClass;
                import com.dsl.classgen.annotations.GeneratedInnerClass;
                import com.dsl.classgen.annotations.InnerField;
                import com.dsl.classgen.annotations.PrivateConstructor;
                
                /*
                 *	*** DO NOT DELETE THE COMMENT BELOW! ***
                 */ 
                @%2$s
                public final class %3$s {
                
                	@%4$s
                	private %3$s() {}
                	
                	// PROPS-FILE-START
                	%5$s
                	// PROPS-FILE-END
                }
                """; 
        
        pathsCtx.setGeneratedClass(String.format(outterClassPattern, pathsCtx.getPackageClass(),
        	GeneratedClass.class.getSimpleName(), 
        	outterClassName, 
        	PrivateConstructor.class.getSimpleName(), 
        	flagsCtx.getIsSingleFile() ? innerStaticClassGenerator.generateInnerStaticClass() : 
        		pathsCtx.getFileList()
	        			.stream()
	        			.map(path -> {
					        Reader.loadPropFile(path);
					        return innerStaticClassGenerator.generateInnerStaticClass();
	        			}).collect(Collectors.joining("\n\t"))));
    }
}