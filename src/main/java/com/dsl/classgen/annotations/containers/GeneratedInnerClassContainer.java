package com.dsl.classgen.annotations.containers;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface GeneratedInnerClassContainer {
    public GeneratedInnerClass[] value();
}

