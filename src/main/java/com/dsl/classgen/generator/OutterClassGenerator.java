package com.dsl.classgen.generator;

import java.util.List;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedOutterClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public final class OutterClassGenerator extends SupportProvider {

	public void generateData() {
        LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new outter class...\n");
        formatGenerationOutput("Outter Class", pathsCtx.getOutterClassName(), "\n");

        CompilationUnit outterClassUnit = generateOutterClass();
		outterClassUnit.getClassByName(pathsCtx.getOutterClassName()).ifPresent(c ->generateInnerClasses().forEach(c::addMember));
		pathsCtx.setGeneratedClass(outterClassUnit.toString());
	}
	
	private CompilationUnit generateOutterClass() {
		CompilationUnit cUnit = new CompilationUnit();
        
        cUnit.setPackageDeclaration(pathsCtx.getPackageClass())
        	 .addImport(GeneratedOutterClass.class)
        	 .addImport(GeneratedPrivateConstructor.class)
        	 .addImport(GeneratedInnerStaticClass.class)
        	 .addImport(GeneratedInnerField.class)
        	 .addClass(pathsCtx.getOutterClassName(), Keyword.PUBLIC, Keyword.FINAL)
        	 .addAnnotation(GeneratedOutterClass.class)
        	 .addConstructor(Keyword.PRIVATE)
        	 .addAnnotation(GeneratedPrivateConstructor.class);
        
        return cUnit;
	}
	
	private List<ClassOrInterfaceDeclaration> generateInnerClasses() {
		var innerClassGenerator = new InnerStaticClassGenerator();
		
        return OutterClassModel.getMapModelStream()
        						.map(innerClassGenerator::generateData)
	        					.toList();
	}
}
