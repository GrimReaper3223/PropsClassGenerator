package com.dsl.classgen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dsl.classgen.annotations.containers.InnerFieldContainer;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(InnerFieldContainer.class)
public @interface InnerField {
	String key();			// a chave deve ser somente a chave que contem o valor no arquivo de propriedades
	int hash() default 0;	// o hash deve ser o par chave-valor correspondente do arquivo de propriedades
}
