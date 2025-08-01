package com.dsl.test.classgen.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.test.classgen.HarnessTestTools;

class TestModelCreation implements HarnessTestTools {

	@Test
	void testModelCreation() throws IOException {
		try (Stream<Path> files = Files.list(inPropsPath)) {
			files.forEach(path -> System.out.println(InnerStaticClassModel.initInstance(path)));
		}
	}
}
