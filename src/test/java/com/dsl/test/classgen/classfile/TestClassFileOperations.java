package com.dsl.test.classgen.classfile;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dsl.test.classgen.HarnessTestTools;

@SuppressWarnings("preview")
class TestClassFileOperations implements HarnessTestTools {

	private static final Logger LOGGER = LogManager.getLogger(TestClassFileOperations.class);
	
	@Test
	@Disabled("null")
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
	
	@Test
//	@Disabled("Testes indicaram sucesso na eliminacao de um campo e sua anotacao no bytecode da classe P$FxButton.class. Esta funcionalidade esta pronta para ser implementada.")
	void testEraseInnerFieldFromClassFile() throws IOException {
		String fullClassName = "target/test-classes/com/dsl/test/classgen/generated/P$FxButton.class";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(Path.of(fullClassName));
		String fieldNameToCompare = "fx.button.close";
		
		ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm && fm.fieldName().stringValue().toLowerCase().replaceAll("_", ".").contains(fieldNameToCompare));
		byte[] newBytes = cf.transform(cm, ct);
		
		try(OutputStream out = Files.newOutputStream(Path.of(System.getProperty("user.dir"), "test.class"))) {
			out.write(newBytes);
		}
	}
	
	void testInsertInnerFieldInClassFile() throws IOException {
		String fullClassName = "target/test-classes/com/dsl/test/classgen/generated/P$FxButton.class";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(Path.of(fullClassName));
		
		
	}
}