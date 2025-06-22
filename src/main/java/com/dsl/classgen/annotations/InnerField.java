package com.dsl.classgen.annotations;

import com.dsl.classgen.annotations.containers.InnerFieldContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@Repeatable(value=InnerFieldContainer.class)
public @interface InnerField {
    public String key();

    public int hash() default 0;
}

