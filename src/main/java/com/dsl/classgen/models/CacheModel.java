package com.dsl.classgen.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
				Utils.handleException(e);
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

		CacheManager.queueNewFileToCreateCache(this.filePath);
	}

	/**
	 * @param obj the obj
	 * @return true, if some attributes is equals to other object instace
	 */
	@Override
	public boolean equals(Object obj) {
		if(Objects.nonNull(obj)) {
			CacheModel cm = CacheModel.class.cast(obj);
			return cm.fileHash == this.fileHash && cm.entries.equals(this.entries);
		}
		throw new NullPointerException("** BUG **: The object instance is null.");
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return String.format("""
				File Path: %s,
				File Hash: %d,
				Java Type: %s,
				Properties: %n%s
				""", filePath,
				fileHash,
				javaType,
				entries.entrySet().stream()
						.map(entry -> String.format("\t%s : (%d)", entry.getValue().toString(), entry.getKey()))
						.collect(Collectors.joining("\n")));
	}
}
