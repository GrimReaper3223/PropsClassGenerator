package com.dsl.classgen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotations.containers.GeneratedClassContainer;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(GeneratedClassContainer.class)
public @interface GeneratedClass{}
