package com.dsl.test.classgen.classfile;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.dsl.test.classgen.HarnessTestTools;

class TestClassFileOperations implements HarnessTestTools {

	private static final Logger LOGGER = LogManager.getLogger(TestClassFileOperations.class);
	
	@Test
	@SuppressWarnings("preview")
	void readClassFile() throws IOException {
		String fullClassName = "com/dsl/test/classgen/generated/P";
		String className = "FxTable";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(classPath);
		
		cm.attributes()
		  .stream()
		  .filter(InnerClassesAttribute.class::isInstance)
		  .map(InnerClassesAttribute.class::cast)
		  .flatMap(attr -> attr.classes().stream())
		  .<InnerClassInfo>mapMulti((elem, consumer) -> {
				try {
					if (elem.outerClass().orElseThrow().name().equalsString(fullClassName)) {
						consumer.accept(elem);
					}
				} catch (NoSuchElementException _) {
					// exception not treated
				}
		  })
		  .collect(Collectors.collectingAndThen(Collectors.toSet(), classInfo -> {
				classInfo.stream()
						 .filter(e -> e.innerName().get().equalsString(className))
						 .flatMap(e -> {
								ClassModel model = null;
								try {
									model = cf.parse(Path.of("target/test-classes", e.innerClass().name().stringValue() + ".class"));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								return model.fields().stream();
						 })
						 .forEach(LOGGER::debug);
				return null;
		  }));
	}
}
