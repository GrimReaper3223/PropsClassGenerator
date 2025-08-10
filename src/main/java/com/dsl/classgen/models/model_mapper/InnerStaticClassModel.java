package com.dsl.classgen.models.model_mapper;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.models.Parsers;
import com.dsl.classgen.utils.Utils;
import com.github.javaparser.ast.Modifier.Keyword;

/**
 * The Record InnerStaticClassModel.
 *
 * @param annotationMetadata the annotation metadata
 * @param fieldModelList     the field model list
 * @param className          the class name
 * @param sourceModifiers    the source modifiers
 * @param byteCodeModifiers  the byte code modifiers
 */
public record InnerStaticClassModel(ClassAnnotationModel annotationMetadata, List<InnerFieldModel> fieldModelList,
		String className, Keyword[] sourceModifiers, int byteCodeModifiers) implements Parsers {

	/**
	 * Inits the instance.
	 *
	 * @param <T> the generic type to be associated with the argument (String or Path)
	 * @param filePath the property file path
	 * @return the instance of this class
	 */
	public static <T> InnerStaticClassModel initInstance(T filePath) {
		Path path = Path.of(filePath.toString());
		String formattedClassName = new Parsers() {}.parseClassName(filePath);
		Keyword[] sourceFlags = { Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL };
		int byteCodeFlags = ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER;

		InnerStaticClassModel model = null;

		try {
			var annotation = initAnnotation(path);
			model = new InnerStaticClassModel(annotation, initFieldList(path, annotation.javaType()),
					formattedClassName, sourceFlags, byteCodeFlags);
		} catch (InterruptedException | ExecutionException e) {
			Utils.logException(e);
		}

		OutterClassModel.computeClassModelToMap(model);

		return model;
	}

	/**
	 * Insert new model.
	 *
	 * @param propertiesKey   the properties key
	 * @param propertiesValue the properties value
	 * @param fieldType       the field type (a.k.a javaType, declared in the properties file)
	 * @return the new inner field model
	 */
	public InnerFieldModel insertNewModel(String propertiesKey, Object propertiesValue, Class<?> fieldType) {
		FieldAnnotationModel annotation = new FieldAnnotationModel(propertiesKey,
				Objects.hash(propertiesKey, propertiesValue));
		String formattedFieldName = parseFieldName(propertiesKey);
		InnerFieldModel model = new InnerFieldModel(annotation, fieldType, formattedFieldName, propertiesValue);

		this.fieldModelList.add(model);
		CacheManager.getModelFromCacheMap(this.annotationMetadata.filePath()).computeFieldInEntryMap(model);
		return model;
	}

	/**
	 * Delete model.
	 *
	 * @param model the model to be deleted
	 */
	public void deleteModel(InnerFieldModel model) {
		this.fieldModelList.remove(model);
	}

	/**
	 * Inits the annotation.
	 *
	 * @param filePath the properties file path
	 * @return the class annotation model
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException   the execution exception
	 */
	private static ClassAnnotationModel initAnnotation(Path filePath) throws InterruptedException, ExecutionException {
		String extractedJavaType = Reader.readJavaType(filePath);
		extractedJavaType = extractedJavaType.endsWith(".class")
				? extractedJavaType.substring(0, extractedJavaType.indexOf("."))
				: extractedJavaType;
		Class<?> javaType = null;

		try {
    		if ((javaType = Class.forPrimitiveName(extractedJavaType)) == null) {
    			javaType = Class.forName(extractedJavaType);
    		}
		} catch (ClassNotFoundException _) {
			try {
				javaType = Class.forName("java.lang." + extractedJavaType);
			} catch (ClassNotFoundException e1) {
				Utils.logException(e1);
			}
		}
		return new ClassAnnotationModel(staticHashCode(filePath), filePath, javaType);
	}

	/**
	 * Inits the field list.
	 *
	 * @param filePath  the properties file path
	 * @param fieldType the field type (a.k.a javaType, declared in the properties file)
	 * @return the list
	 */
	private static List<InnerFieldModel> initFieldList(Path filePath, Class<?> fieldType) {
		return Reader.loadProp(filePath).entrySet().stream().map(entry -> {
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			FieldAnnotationModel annotation = new FieldAnnotationModel(key, Objects.hash(key, value));
			String formattedFieldName = new Parsers() {}.parseFieldName(key);
			return new InnerFieldModel(annotation, fieldType, formattedFieldName, value);
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Static call for hash code method calculation.
	 *
	 * @param filePath the properties file path
	 * @return the hash of the file attributes
	 */
	private static int staticHashCode(Path filePath) {
		FileTime creationTime = FileTime.fromMillis(0L);
		FileTime lastModifiedTime = FileTime.fromMillis(0L);
		long fileSize = 0L;

		try {
			BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath.toString()), BasicFileAttributes.class);
			creationTime = attrs.creationTime();
			lastModifiedTime = attrs.lastModifiedTime();
			fileSize = attrs.size();
		} catch (IOException e) {
			Utils.logException(e);
		}

		return Objects.hash(creationTime.toMillis(), lastModifiedTime.toMillis(), fileSize);
	}

	/**
	 * @param obj the obj
	 * @return true, if is model properties equals
	 */
	@Override
	public boolean equals(Object obj) {
		InnerStaticClassModel iscm = InnerStaticClassModel.class.cast(Objects.requireNonNull(obj));
		var hashList1 = iscm.fieldModelList.stream().map(model -> model.annotationMetadata().hash()).toList();
		var hashList2 = fieldModelList.stream().map(model -> model.annotationMetadata().hash()).toList();
		return iscm.annotationMetadata.hash() == annotationMetadata.hash() && hashList1.containsAll(hashList2);
	}

	/**
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return staticHashCode(annotationMetadata.filePath());
	}
}
