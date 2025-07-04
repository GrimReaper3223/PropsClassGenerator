package com.dsl.classgen.annotations.containers;

import com.dsl.classgen.annotations.PrivateConstructor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.CONSTRUCTOR})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface PrivateConstructorContainer {
    public PrivateConstructor[] value();
}

