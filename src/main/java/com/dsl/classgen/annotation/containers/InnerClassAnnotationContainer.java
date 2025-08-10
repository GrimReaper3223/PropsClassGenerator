package com.dsl.classgen.annotation.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotation.GeneratedInnerStaticClass;

@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface InnerClassAnnotationContainer {
	public GeneratedInnerStaticClass[] value();
}
