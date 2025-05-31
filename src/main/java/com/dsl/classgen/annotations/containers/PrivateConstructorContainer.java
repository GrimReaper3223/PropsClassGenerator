package com.dsl.classgen.annotations.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotations.PrivateConstructor;

@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivateConstructorContainer {
	PrivateConstructor[] value();
}
