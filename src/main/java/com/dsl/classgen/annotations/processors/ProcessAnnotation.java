package com.dsl.classgen.annotations.processors;

import java.util.Arrays;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.io.Reader;

public class ProcessAnnotation {
	
	/*
	 * Para fazer o processamento, precisamos de duas coisas:
	 * 
	 * 1 - iterar pela classe P, colhendo todas as anotacoes das classes internas estaticas
	 * 2 - iterar sobre cada classe interna estatica e colher as anotacoes de seus campos
	 * 
	 * Apos esses passos, devemos armazenar em um mapa cuja chave e a anotacao
	 * da classe interna estatica e cujo valor e uma lista com todas as anotacoes
	 * de todos os campos anotados para esta classe.
	 * 
	 * Com esses dados processados, devemos identificar se o caminho do arquivo
	 * de propriedades coincide com o que esta no cache. 
	 */
	
	public static void processAnnotations() {
		System.out.println('\n');
		Arrays.stream(Reader.loadGeneratedBinClass().getDeclaredClasses())
			  .flatMap(cl -> {
				  System.out.println(cl.getAnnotation(GeneratedInnerClass.class));
				  return Arrays.stream(cl.getDeclaredFields()).flatMap(f -> Arrays.stream(f.getDeclaredAnnotations()));
			  })
			  .forEach(a -> {
				  System.out.println(a);  
			  });
	}
}
