package com.dsl.classgen.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.utils.Utils;

public class CacheModel implements Serializable {

	private static final long serialVersionUID = 1L;
	public transient Class<?> javaType;
	
	public String filePath;
	public int fileHash;
	public String stringJavaType;
	public Map<Integer, CachePropertiesData> entries;
	
	public CacheModel() {}
	
	public CacheModel(InnerStaticClassModel model) {
		entries = new HashMap<>();
		filePath = model.annotationMetadata().filePath().toString();
		fileHash = model.annotationMetadata().hash();
		javaType = model.annotationMetadata().javaType();
		stringJavaType = javaType.getName();
		model.fieldModelList()
			 .stream()
			 .forEach(instance -> {
				 var annotation = instance.annotationMetadata(); 
				 entries.put(annotation.hash(),
	 						new CachePropertiesData(annotation.key(), 
													instance.fieldValue()));
			 });
	}
	
	public void parseJavaType() {
		if((javaType = Class.forPrimitiveName(stringJavaType)) == null) {
			try {
				javaType = Class.forName(stringJavaType);
			} catch (ClassNotFoundException e) {
				Utils.logException(e);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		CacheModel cm = CacheModel.class.cast(Objects.requireNonNull(obj));
    	return cm.fileHash == this.fileHash && cm.entries.equals(this.entries);
	}
}
