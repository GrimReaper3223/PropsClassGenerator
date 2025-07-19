package com.dsl.classgen.generator;

import java.util.List;
import java.util.stream.Collectors;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedOutterClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.utils.Levels;

public final class OutterClassGenerator extends SupportProvider implements Parsers, Initializer {
	
	private final InnerStaticClassGenerator innerStaticClassGenerator = new InnerStaticClassGenerator();
	private String outterClassName = pathsCtx.getOutterClassName();
	private String imports;
	
	@Override
	public void initClass() {
		outterClassName = pathsCtx.getOutterClassName();
		LOGGER.log(Levels.NOTICE.getLevel(), "Generating classes...\n");
        formatGenerationOutput("Outter Class", outterClassName, "\n");
        imports = createImports(List.of(
    			GeneratedOutterClass.class,
    			GeneratedInnerStaticClass.class,
    			GeneratedInnerField.class,
    			GeneratedPrivateConstructor.class));
	}
	
    public void generateOutterClass() {
        initClass();
    	
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
                	
                	// PROPS-FILE-START
                	%6$s
                	// PROPS-FILE-END
                }
                """, 
                pathsCtx.getPackageClass(),
	        	imports,
	        	GeneratedOutterClass.class.getSimpleName(),
	        	outterClassName, 
	        	GeneratedPrivateConstructor.class.getSimpleName(), 
	        	flagsCtx.getIsSingleFile() ? innerStaticClassGenerator.generateInnerStaticClass() : 
	        		pathsCtx.getFileList()
		        			.stream()
		        			.map(path -> {
						        Reader.loadPropFile(path);
						        return innerStaticClassGenerator.generateInnerStaticClass();
		        			}).collect(Collectors.joining("\n\t"))));
    }
}