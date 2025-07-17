package com.dsl.classgen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotation.containers.PrivateConstructorAnnotationContainer;

@Target(value={ElementType.CONSTRUCTOR})
@Retention(value=RetentionPolicy.RUNTIME)
@Repeatable(value=PrivateConstructorAnnotationContainer.class)
public @interface GeneratedPrivateConstructor {
}

