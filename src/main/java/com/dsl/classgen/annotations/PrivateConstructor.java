package com.dsl.classgen.annotations;

import com.dsl.classgen.annotations.containers.PrivateConstructorContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.CONSTRUCTOR})
@Retention(value=RetentionPolicy.RUNTIME)
@Repeatable(value=PrivateConstructorContainer.class)
public @interface PrivateConstructor {
}

