package com.dsl.classgen.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.models.model_mapper.InnerFieldModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.utils.Utils;

public class CacheModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String filePath;
	public int fileHash;
	public String javaType;
	public Map<Integer, CachePropertiesData> entries;
	
	public CacheModel() {}
	
	public CacheModel(InnerStaticClassModel model) {
		entries = new HashMap<>();
		filePath = model.annotationMetadata().filePath().toString();
		fileHash = model.annotationMetadata().hash();
		javaType = model.annotationMetadata().javaType().getName();
		model.fieldModelList()
			 .stream()
			 .forEach(instance -> {
				 var annotation = instance.annotationMetadata(); 
				 entries.put(annotation.hash(),
	 						new CachePropertiesData(annotation.key(), 
													instance.rawFieldValue(), 
													instance.parsedFieldValue()));
			 });
		
		CacheManager.computeCacheModelToMap(model.annotationMetadata().filePath(), this);
	}
	
	public Class<?> parseJavaType() {
		Class<?> cls = Class.forPrimitiveName(javaType);
		if(cls == null) {
			try {
				cls = Class.forName(javaType);
			} catch (ClassNotFoundException e) {
				Utils.logException(e);
			}
		}
		return cls;
	}
	
	public void computeFieldInEntryMap(InnerFieldModel model) {
		CachePropertiesData cachePropData = new CachePropertiesData(model.annotationMetadata().key(),
				model.rawFieldValue(), model.parsedFieldValue());

		if (entries.computeIfPresent(model.annotationMetadata().hash(), (_, _) -> cachePropData) == null) {
			entries.put(model.annotationMetadata().hash(), cachePropData);
		}
		
		CacheManager.queueNewCacheFile(this.filePath);
	}
	
	@Override
	public boolean equals(Object obj) {
		CacheModel cm = CacheModel.class.cast(Objects.requireNonNull(obj));
    	return cm.fileHash == this.fileHash && cm.entries.equals(this.entries);
	}
	
	// NOTE: este metodo nao deve ser usado
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
