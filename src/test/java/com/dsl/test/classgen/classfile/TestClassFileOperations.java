package com.dsl.test.classgen.classfile;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.annotation.GeneratedInnerField;
import com.dsl.classgen.annotation.GeneratedInnerStaticClass;
import com.dsl.classgen.annotation.GeneratedPrivateConstructor;
import com.dsl.test.classgen.HarnessTestTools;

class TestClassFileOperations implements HarnessTestTools {

	private static final Logger LOGGER = LogManager.getLogger(TestClassFileOperations.class);

	@Test
	@Disabled("null")
	void readClassFile() throws IOException {
		String fullClassName = "com/dsl/test/classgen/generated/P";
		String className = "FxTable";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(classPath);

		cm.attributes().stream().filter(InnerClassesAttribute.class::isInstance).map(InnerClassesAttribute.class::cast)
				.flatMap(attr -> attr.classes().stream()).<InnerClassInfo>mapMulti((elem, consumer) -> {
					try {
						if (elem.outerClass().orElseThrow().name().equalsString(fullClassName)) {
							consumer.accept(elem);
						}
					} catch (NoSuchElementException _) {
						// exception not treated
					}
				}).collect(Collectors.collectingAndThen(Collectors.toSet(), classInfo -> {
					classInfo.stream().filter(e -> e.innerName().get().equalsString(className)).flatMap(e -> {
						ClassModel model = null;
						try {
							model = cf.parse(
									Path.of("target/test-classes", e.innerClass().name().stringValue() + ".class"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return model.fields().stream();
					}).flatMap(
							f -> f.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().stream())
							.forEach(LOGGER::debug);
					return null;
				}));
	}

	@Test
	@Disabled("Testes indicaram sucesso na eliminacao de um campo e sua anotacao no bytecode da classe P$FxButton.class. Esta funcionalidade esta pronta para ser implementada.")
	void testEraseInnerFieldFromClassFile() throws IOException {
		String fullClassName = "target/test-classes/com/dsl/test/classgen/generated/P$FxButton.class";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(Path.of(fullClassName));
		String fieldNameToCompare = "fx.button.close";

		ClassTransform ct = ClassTransform.dropping(elem -> elem instanceof FieldModel fm
				&& fm.fieldName().stringValue().toLowerCase().replaceAll("_", ".").contains(fieldNameToCompare));
		byte[] newBytes = cf.transformClass(cm, ct);

		try (OutputStream out = Files.newOutputStream(Path.of(System.getProperty("user.dir"), "test.class"))) {
			out.write(newBytes);
		}
	}

	@Test
	@Disabled("Pronto para implementacao")
	void testInsertInnerFieldInClassFile() throws IOException {
		String fullClassName = "target/test-classes/com/dsl/test/classgen/generated/P$FxButton.class";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(Path.of(fullClassName));

		byte[] newBytes = cf.build(cm.thisClass().asSymbol(), classBuilder -> {
			cm.elementStream().forEach(classBuilder::accept);

			classBuilder.withField("TEST_FIELD", ConstantDescs.CD_String,
					fieldBuilder -> fieldBuilder.with(buildAnnotation()).with(ConstantValueAttribute.of("test value"))
							.withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL));
		});

		Files.write(Path.of(System.getProperty("user.dir"), "test.class"), newBytes);
	}

	RuntimeVisibleAnnotationsAttribute buildAnnotation() {
		return RuntimeVisibleAnnotationsAttribute.of(Annotation.of(GeneratedInnerField.class.describeConstable().get(),
				List.of(AnnotationElement.ofString("key", "test.field"), AnnotationElement.ofInt("hash", 1234567890))));
	}

	@Test
	@Disabled("Pronto para implementacao")
	void testFullInnerClassGeneration() throws IOException {
		String baseClassName = "target/test-classes/com/dsl/test/classgen/generated/P.class";
		ClassFile cf = ClassFile.of();
		ClassModel cm = cf.parse(Path.of(baseClassName));

		ClassModel innerClassFileModel = cf.parse(cf.build(ClassDesc.of("Test"), handler -> {
			handler.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL)
					.withSuperclass(cm.thisClass().asSymbol())
					.with(RuntimeVisibleAnnotationsAttribute
							.of(Annotation.of(GeneratedInnerStaticClass.class.describeConstable().orElseThrow())))
					.withMethod(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PRIVATE, h -> {
						h.with(RuntimeVisibleAnnotationsAttribute.of(
								Annotation.of(GeneratedPrivateConstructor.class.describeConstable().orElseThrow())));
					}).withField("TEST_FIELD", ConstantDescs.CD_String, fieldBuilder -> {
						fieldBuilder.with(buildAnnotation()).with(ConstantValueAttribute.of("test value"))
								.withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL);
					});
		}));

		var attr = InnerClassesAttribute.of(
				InnerClassInfo.of(innerClassFileModel.thisClass().asSymbol(), Optional.of(cm.thisClass().asSymbol()),
						Optional.of(innerClassFileModel.thisClass().name().stringValue()),
						ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL));

		byte[] newBytes = cf.build(cm.thisClass().asSymbol(), classBuilder -> {
			cm.elementList().forEach(classBuilder::accept);
			classBuilder.with(attr);
		});

		try (OutputStream out = Files.newOutputStream(Path.of(System.getProperty("user.dir"), "test.class"))) {
			out.write(newBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}