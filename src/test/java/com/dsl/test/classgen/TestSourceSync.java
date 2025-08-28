package com.dsl.test.classgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestSourceSync implements HarnessTestTools {

	static String startPattern, endPattern;
	static String originalValue, changedValue;

	static StringBuilder sourceBuffer;

	@BeforeAll
	static void initVars() {
		startPattern = "// CLASS HINT ~>> src/test/resources/values/strings/javafx-messages.properties";
		endPattern = "// CLASS HINT <<~ src/test/resources/values/strings/javafx-messages.properties";
		sourceBuffer = new StringBuilder();
		originalValue = null;
		changedValue = null;
	}

	@Test
	@Order(1)
	@DisplayName("Is exists source file")
	void testIfIsExistsSourceFile() {
		assertTrue(Files.exists(sourceFilePath));
	}

	@Test
	@Order(2)
	@DisplayName("Is buffer not empty")
	void testIfIsBufferNotEmpty() {
		try (Stream<String> lines = Files.lines(sourceFilePath)) {
			lines.map(line -> line.concat("\n")).forEach(sourceBuffer::append);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			assertFalse(sourceBuffer.isEmpty());
		}
	}

	@Test
	@Order(3)
	@DisplayName("Is changes successfully")
	void testIfIsChangesSuccessfully() {
		originalValue = sourceBuffer.toString();
		changedValue = sourceBuffer
				.delete(sourceBuffer.indexOf(startPattern), sourceBuffer.indexOf(endPattern) + endPattern.length() + 1)
				.toString();

		assertNotSame(originalValue, changedValue);
		printResults();
	}

	void printResults() {
		System.out.println("**********************");
		System.out.println("*** ORIGINAL VALUE ***");
		System.out.println("**********************\n");
		System.out.println(originalValue);
		System.out.println("\n**********************");
		System.out.println("*** CHANGED VALUE ***");
		System.out.println("**********************\n");
		System.out.println(changedValue);
	}

	@Test
	void testReplacement() {
		System.out.println(sourceFilePath.toString());
		System.out.println(sourceFilePath.toString().replaceAll("(.*/java/)", ""));
	}
}
