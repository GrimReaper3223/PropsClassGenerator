package com.dsl.classgen.annotations.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotations.GeneratedInnerClass;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedInnerClassContainer {
	GeneratedInnerClass[] value();
}
