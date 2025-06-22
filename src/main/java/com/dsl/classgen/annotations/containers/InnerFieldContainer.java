package com.dsl.classgen.annotations.containers;

import com.dsl.classgen.annotations.InnerField;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface InnerFieldContainer {
    public InnerField[] value();
}

