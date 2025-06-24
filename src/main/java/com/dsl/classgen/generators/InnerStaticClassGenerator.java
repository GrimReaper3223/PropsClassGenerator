package com.dsl.classgen.generators;

import com.dsl.classgen.annotations.GeneratedInnerClass;
import com.dsl.classgen.annotations.PrivateConstructor;
import com.dsl.classgen.io.HashTableModel;
import com.dsl.classgen.io.Values;
import com.dsl.classgen.utils.Utils;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class InnerStaticClassGenerator implements OutputLogGeneration {
	
    public String generateInnerStaticClass() {
        InnerFieldGenerator innerFieldGenerator = new InnerFieldGenerator();
        String softPropertyFileName = Values.getSoftPropertiesFileName();
        String formattedClassName = formatClassName(softPropertyFileName);
        HashTableModel htm = Values.getElementFromHashTableMap(Utils.resolveJsonFullPath(softPropertyFileName));
        
        formatGenerationOutput("Static Inner Class", formattedClassName, null);
        
        return String.format("""
        		@%1$s(filePath = \"%2$s\", javaType = %3$s.class, hash = %4$d)
        		\tpublic static final class %5$s {
        		
	        		\t@%6$s
	        		\tprivate %5$s() {}
	        		
	        		\t%7$s
        		\t}
        		""", GeneratedInnerClass.class.getSimpleName(), 
        				Values.isSingleFile() ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().resolve(Values.getRawPropertiesfileName()), 
        				Values.getPropertiesDataType(),
        				htm.fileHash,
        				formattedClassName, 
        				PrivateConstructor.class.getSimpleName(), 
        				Values.getProps()
        					  .entrySet()
        					  .stream()
        					  .map(entry -> {
						          String key = entry.getKey().toString();
						          String value = entry.getValue().toString();
						          
						      	/*
						      	 * TODO: key se refere a chave que guarda um valor no arquivo de propriedade.
						      	 * Esta chave e usada para pesquisar o valor de hash da chave + valor 
						      	 * da propriedade no hashTableMap.
						      	 * 
						      	 * Se a chave da propriedade nao for encontrada dentro de hashTableMap por algum motivo,
						      	 * null sera retornado, o que pode gerar um erro, ja que int nao pode guardar nulos.
						      	 * 
						      	 * O hashTableMap referido e uma construcao em tempo de execucao do arquivo de propriedades carregado.
						      	 * Devemos decidir, por assim dizer, se vamos extrair diretamente a chave do hashTableMap
						      	 * ou da propriedade... ou se devemos manter o modelo atual, do qual pode retornar um valor nulo
						      	 * e tratar isso da melhor forma no metodo generateInnerField
						      	 */
						          Integer hash = htm.hashTableMap.get(key);
						          return innerFieldGenerator.generateInnerField(key, value, hash);
        					  }).collect(Collectors.joining("\n\t\t")));
    }

    private String formatClassName(String className) {
        String regex = "[\\s\\Q`!@#$%^&*()_+{}:\"<>?|\\~/.;',[]=-\\E]+";
        return Arrays.stream(className.split(regex))
        			 .map(token -> token.replaceFirst(Character.toString(token.charAt(0)), Character.toString(Character.toUpperCase(token.charAt(0)))))
        			 .collect(Collectors.joining());
    }
}

