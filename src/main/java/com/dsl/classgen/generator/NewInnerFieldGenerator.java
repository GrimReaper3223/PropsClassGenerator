package com.dsl.classgen.generator;

import java.util.List;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.models.model_mapper.InnerFieldModel;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TypeExpr;

public final class NewInnerFieldGenerator extends SupportProvider {

	public List<FieldDeclaration> generateData(List<InnerFieldModel> models) {
		return models.stream()
					  .map(model -> {
						  formatGenerationOutput("Inner Field", model.annotationMetadata().key(), null);
						  return generateInnerField(model);
					  }).toList();
    }
	
	private FieldDeclaration generateInnerField(InnerFieldModel model) {
		var fieldDecl = new FieldDeclaration();
		fieldDecl.addModifier(model.sourceModifiers())
				 .addAnnotation(new NormalAnnotationExpr().addPair("key", new StringLiteralExpr(model.annotationMetadata().key()))
						.addPair("hash", new IntegerLiteralExpr(String.valueOf(model.annotationMetadata().hash())))
						.setName(GeneratedInnerField.class.getSimpleName()))
				 .addVariable(new VariableDeclarator(new TypeExpr().setType(model.fieldType()).getType(), 
						 model.fieldName()));
		
		fieldDecl.getVariable(0).setInitializer(model.fieldValue().toString());
				 
		return fieldDecl;
	}
}
