package com.dsl.classgen.generator;

import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.TypeParameter;

public final class InnerStaticClassGenerator extends SupportProvider {

	public ClassOrInterfaceDeclaration generateData(InnerStaticClassModel model) {
        LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new inner static class...\n");
        formatGenerationOutput("Static Inner Class", model.className(), null);
        return generateInnerStaticClass(model);
	}
	
	private ClassOrInterfaceDeclaration generateInnerStaticClass(InnerStaticClassModel model) {
		InnerFieldGenerator fieldGenerator = new InnerFieldGenerator();
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration();
		classDecl.addModifier(model.sourceModifiers())
				 .setName(model.className())
				 .addAnnotation(new NormalAnnotationExpr().addPair("filePath", new StringLiteralExpr(model.annotationMetadata().filePath().toString()))
						 .addPair("hash", new IntegerLiteralExpr(String.valueOf(model.annotationMetadata().hash())))
						 .addPair("javaType", new ClassExpr(new TypeParameter(model.annotationMetadata().javaType().getSimpleName())))
						 .setName(GeneratedInnerStaticClass.class.getSimpleName()))
				 .addConstructor(Keyword.PRIVATE)
				 .addAnnotation(GeneratedPrivateConstructor.class);
		
		fieldGenerator.generateData(model.fieldModelList()).forEach(classDecl::addMember);
		return classDecl;
	}
}
