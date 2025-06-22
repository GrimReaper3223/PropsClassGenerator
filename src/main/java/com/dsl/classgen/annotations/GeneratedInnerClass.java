package com.dsl.classgen.annotations;

import com.dsl.classgen.annotations.containers.GeneratedInnerClassContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Repeatable(value=GeneratedInnerClassContainer.class)
public @interface GeneratedInnerClass {
    public String filePath();

    public Class<?> javaType() default Object.class;

    public int hash() default 0;
}

