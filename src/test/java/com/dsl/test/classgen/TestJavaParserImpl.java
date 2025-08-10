package com.dsl.test.classgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedOutterClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.TypeParameter;

class TestJavaParserImpl implements HarnessTestTools {

	@Test
	void testASTParser() throws IOException {
		CompilationUnit cu1 = new CompilationUnit();
		CompilationUnit cu2 = new CompilationUnit();

		cu2.addClass("TestProp", Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL)
				.addAnnotation(new NormalAnnotationExpr().addPair("testKey", new StringLiteralExpr("test value"))
						.addPair("hash", new IntegerLiteralExpr("12345"))
						.addPair("javaType", new ClassExpr(new TypeParameter("String")))
						.setName(GeneratedInnerStaticClass.class.getSimpleName()))
				.addConstructor(Keyword.PRIVATE)
				.addAnnotation(new NormalAnnotationExpr().setName(GeneratedPrivateConstructor.class.getSimpleName()));

		cu2.getClassByName("TestProp").ifPresent(c -> {
			c.addFieldWithInitializer(String.class, "TEST_FIELD", new StringLiteralExpr("com.test.value"),
					Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL)
					.addAnnotation(new NormalAnnotationExpr().addPair("hash", new IntegerLiteralExpr("12345"))
							.addPair("javaType", new ClassExpr(new TypeParameter("String")))
							.setName(GeneratedInnerField.class.getSimpleName()));
		});

		cu1.setPackageDeclaration("com.dsl.test.classgen.generated").addImport(GeneratedOutterClass.class)
				.addImport(GeneratedPrivateConstructor.class).addImport(GeneratedInnerStaticClass.class)
				.addImport(GeneratedInnerField.class).addClass("P", Keyword.PUBLIC, Keyword.FINAL)
				.addConstructor(Keyword.PRIVATE);

		cu1.getClassByName("P").ifPresent(c -> {
			c.addAnnotation(new NormalAnnotationExpr().addPair("testKey", new StringLiteralExpr("test value"))
					.addPair("hash", new IntegerLiteralExpr("12345"))
					.setName(GeneratedOutterClass.class.getSimpleName()));

			c.getConstructors().forEach(constructor -> {
				constructor.addAnnotation(
						new NormalAnnotationExpr().setName(GeneratedPrivateConstructor.class.getSimpleName()));
			});

			c.addMember(cu2.getClassByName("TestProp").orElseThrow(() -> new IllegalStateException("Class not found")));
		});

		Files.writeString(Path.of(System.getProperty("user.dir"), "test.java"), cu1.toString());
	}
}
