package com.dsl.classgen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotation.containers.InnerFieldAnnotationContainer;

@Target(value = { ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Repeatable(value = InnerFieldAnnotationContainer.class)
public @interface GeneratedInnerField {
	public String key();

	public int hash() default 0;
}
