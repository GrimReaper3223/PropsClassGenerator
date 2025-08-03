package com.dsl.classgen.models.model_mapper;

import java.io.IOException;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.file_manager.Reader;
import com.dsl.classgen.models.Hints;
import com.dsl.classgen.models.Parsers;
import com.dsl.classgen.utils.Utils;
import com.github.javaparser.ast.Modifier.Keyword;

public record InnerStaticClassModel (ClassAnnotationModel annotationMetadata, 
		List<InnerFieldModel> fieldModelList, 
		String className, 
		Keyword[] sourceModifiers,
		AccessFlag[] byteCodeModifiers) implements Hints, Parsers {
	
	public static <T> InnerStaticClassModel initInstance(T filePath) {
		Path path = Path.of(filePath.toString());
		String formattedClassName = new Parsers() {}.parseClassName(filePath);
		Keyword[] sourceFlags = new Keyword[] { Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL };
		AccessFlag[] byteCodeFlags = new AccessFlag[] { AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL };
		
		InnerStaticClassModel model = null;
		
		try {
			var annotation = initAnnotation(path);
			model = new InnerStaticClassModel(annotation, 
					initFieldList(path, annotation.javaType()), 
					formattedClassName, 
					sourceFlags, 
					byteCodeFlags);
		} catch (InterruptedException | ExecutionException e) {
			Utils.logException(e);
		}
		
		return model;
	}
	
	public InnerFieldModel insertNewModel(String propertiesKey, Object propertiesValue, Class<?> fieldType) {
		FieldAnnotationModel annotation = new FieldAnnotationModel(propertiesKey, Objects.hash(propertiesKey, fieldType.cast(propertiesValue)));
		String formattedFieldName = parseFieldName(propertiesKey);
		InnerFieldModel model = new InnerFieldModel(annotation, fieldType, formattedFieldName, propertiesValue);
		this.fieldModelList.add(model);
		return model;
	}
	
	public void deleteModel(InnerFieldModel model) {
		this.fieldModelList.remove(model);
	}
	
	private static ClassAnnotationModel initAnnotation(Path filePath) throws InterruptedException, ExecutionException {
		String extractedJavaType = Reader.readJavaType(filePath);
		extractedJavaType = extractedJavaType.endsWith(".class") ? extractedJavaType.substring(0, extractedJavaType.indexOf(".")) : extractedJavaType;
		Class<?> javaType = null;
		
		try {
			if((javaType = Class.forPrimitiveName(extractedJavaType)) == null) {
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
	
	private static List<InnerFieldModel> initFieldList(Path filePath, Class<?> fieldType) {
		Parsers parser = new Parsers() {};
		return Reader.loadProp(filePath)
			  .entrySet()
			  .stream()
			  .map(entry -> {
				  String key = entry.getKey().toString();
				  Object value = entry.getValue();
				  
				  FieldAnnotationModel annotation = new FieldAnnotationModel(key, Objects.hash(key, value));
				  String formattedFieldName = parser.parseFieldName(key);
				  return new InnerFieldModel(annotation, fieldType, formattedFieldName, value);
			  }).toList();
	}
	
	private static int staticHashCode(Path filePath) {
		FileTime creationTime = FileTime.fromMillis(0L);
        FileTime lastModifiedTime = FileTime.fromMillis(0L);
        long fileSize = 0L;
        
        try {
        	BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath.toString()), BasicFileAttributes.class);
            creationTime = attrs.creationTime();
            lastModifiedTime = attrs.lastModifiedTime();
            fileSize = attrs.size();
        }
        catch (IOException e) {
        	Utils.logException(e);
        }
        
        return Objects.hash(creationTime.toMillis(), lastModifiedTime.toMillis(), fileSize);
	}
	
	@Override
	public String startHint() {
		return String.format("// INNER CLASS HINT ~>> %s", annotationMetadata.filePath());
	}

	@Override
	public String endHint() {
		return String.format("// INNER CLASS HINT <<~ %s", annotationMetadata.filePath());
	}
	
	@Override
	public boolean equals(Object obj) {
		InnerStaticClassModel iscm = InnerStaticClassModel.class.cast(Objects.requireNonNull(obj));
		var hashList1 = iscm.fieldModelList.stream().map(model -> model.annotationMetadata().hash()).toList();
		var hashList2 = fieldModelList.stream().map(model -> model.annotationMetadata().hash()).toList();
    	return iscm.annotationMetadata.hash() == annotationMetadata.hash() && hashList1.containsAll(hashList2);
	}
	
	@Override
	public int hashCode() {
		return staticHashCode(annotationMetadata.filePath());
	}
}
