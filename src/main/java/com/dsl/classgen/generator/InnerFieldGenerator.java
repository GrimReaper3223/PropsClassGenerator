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

/**
 * The Class InnerFieldGenerator.
 */
public final class InnerFieldGenerator extends SupportProvider {

	/**
	 * Public method for calling, without exposing the internal implementation
	 *
	 * @param models the models for generating fields
	 * @return the list of fields generated in field declarations
	 */
	public List<FieldDeclaration> generateData(List<InnerFieldModel> models) {
		return models.stream().map(model -> {
			formatGenerationOutput("Inner Field", model.annotationMetadata().key(), null);
			return generateInnerField(model);
		}).toList();
	}

	/**
	 * Generate inner field.
	 *
	 * @param model the model for generating field
	 * @return the generated field declaration
	 */
	private FieldDeclaration generateInnerField(InnerFieldModel model) {
		var fieldDecl = new FieldDeclaration();
		fieldDecl.addModifier(model.sourceModifiers())
				.addAnnotation(new NormalAnnotationExpr()
						.addPair("key", new StringLiteralExpr(model.annotationMetadata().key()))
						.addPair("hash", new IntegerLiteralExpr(String.valueOf(model.annotationMetadata().hash())))
						.setName(GeneratedInnerField.class.getSimpleName()))
				.addVariable(
						new VariableDeclarator(new TypeExpr().setType(model.fieldType()).getType(), model.fieldName()));

		fieldDecl.getVariable(0).setInitializer(model.parsedFieldValue().toString());
		return fieldDecl;
	}
}
