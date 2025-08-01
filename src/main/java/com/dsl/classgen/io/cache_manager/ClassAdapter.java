package com.dsl.classgen.io.cache_manager;

import java.io.IOException;

import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.utils.Utils;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ClassAdapter extends TypeAdapter<CacheModel>{

	@Override
	public CacheModel read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		Class<?> cls = null;
		try {
			cls = Class.forName(in.nextString());
		} catch (ClassNotFoundException e) {
			Utils.logException(e);
		}
		
		return new CacheModel();
	}

	@Override
	public void write(JsonWriter out, CacheModel value) throws IOException {
		if(value == null) {
			out.nullValue();
			return;
		}
		
		out.value(value.toString());
	}
}
