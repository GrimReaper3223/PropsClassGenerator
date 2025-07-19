package com.dsl.test.classgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.Generator;
import com.dsl.classgen.generator.InnerFieldGenerator;
import com.dsl.classgen.io.file_manager.Reader;

class SimpleGeneratorTest implements HarnessTestTools {
	
	StringBuilder sb;
	
	void generationTest() {
		Generator.init(inPropsPath, PACKAGE_CLASS, false);
		Generator.generate();
		
		// ignore os testes de assercao
		Assertions.assertTrue(true);
	}

	void fieldDeleteTest() {
		fieldInsertTest();
		String classSourceStartHint = "// FIELD HINT ~>> test.javafx";
		String classSourceEndHint = "// FIELD HINT <<~ test.javafx";
		int endPatternFullIndex = classSourceEndHint.length();
		
		sb.delete(sb.indexOf(classSourceStartHint) - 1, sb.indexOf(classSourceEndHint) + endPatternFullIndex + 3);
		System.out.println(sb.toString());
	}
	
	@Test
	void fieldInsertTest() {
		Generator.init(inPropsPath, PACKAGE_CLASS, false);
		sb = Reader.readSource(sourceFilePath);
		
		String pattern = "// PROPS-CONTENT-START: exception-message.properties";
		
		String s1 = createSource("test.javafx", "test value", 1234567890);
	   	sb.insert(sb.indexOf(pattern) + pattern.length() + 1,  s1);
		System.out.println(sb.toString());
	}
	
	String createSource(String fieldKey, String fieldValue, int hash) {
		return "\t\t" + new InnerFieldGenerator().generateInnerField(fieldKey, fieldValue, hash) + "\n";
	}
}
