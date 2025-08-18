package com.dsl.classgen.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.models.model_mapper.InnerFieldModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.utils.Utils;

/**
 * The Class CacheModel.
 */
public class CacheModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public String filePath;
	public int fileHash;
	public String javaType;
	public Map<Integer, CachePropertiesData> entries;

	/**
	 * Instantiates a new cache model. This constructor is used by deserialization
	 * processes and should not be used
	 */
	public CacheModel() {}

	/**
	 * Instantiates a new cache model.
	 *
	 * @param model the existing inner static class model
	 */
	public CacheModel(InnerStaticClassModel model) {
		entries = new HashMap<>();
		filePath = model.annotationMetadata().filePath().toString();
		fileHash = model.annotationMetadata().hash();
		javaType = model.annotationMetadata().javaType().getName();
		model.fieldModelList().stream().forEach(instance -> {
			var annotation = instance.annotationMetadata();
			entries.put(annotation.hash(),
					new CachePropertiesData(annotation.key(), instance.rawFieldValue(), instance.parsedFieldValue()));
		});

		CacheManager.computeCacheModelToMap(model.annotationMetadata().filePath(), this);
	}

	/**
	 * Parses the java type.
	 *
	 * @return the class casted from the java type string
	 */
	public Class<?> parseJavaType() {
		Class<?> cls = Class.forPrimitiveName(javaType);
		if (cls == null) {
			try {
				cls = Class.forName(javaType);
			} catch (ClassNotFoundException e) {
				Utils.logException(e);
			}
		}
		return cls;
	}

	/**
	 * Compute field in entry map.
	 *
	 * @param model the new inner field model to compute in the entry map
	 */
	public void computeFieldInEntryMap(InnerFieldModel model) {
		CachePropertiesData cachePropData = new CachePropertiesData(model.annotationMetadata().key(),
				model.rawFieldValue(), model.parsedFieldValue());

		if (entries.computeIfPresent(model.annotationMetadata().hash(), (_, _) -> cachePropData) == null) {
			entries.put(model.annotationMetadata().hash(), cachePropData);
		}

		CacheManager.queueNewCacheFile(this.filePath);
	}

	/**
	 * @param obj the obj
	 * @return true, if some attributes is equals to other object instace
	 */
	@Override
	public boolean equals(Object obj) {
		CacheModel cm = CacheModel.class.cast(Objects.requireNonNull(obj));
		return cm.fileHash == this.fileHash && cm.entries.equals(this.entries);
	}

	/**
	 * NOTE: This method should not be used. It is overridden to avoid
	 * 
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
