package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.Levels;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class Writer extends SupportProvider {
	
    private Writer() {}

    public static void write() {
        Path outputPackagePath = pathsCtx.getOutputSourceDirPath();
        Path outputFilePath = pathsCtx.getOutputSourceFilePath();
        
        // se nao existir o pacote de saida, entao os diretorios e o arquivo gerado serao criados
        // se o pacote e saida existir, mas nao existir o arquivo de saida, entao somente ele sera gerado
        // caso contrario, o arquivo sera marcado como existente e o metodo retorna sua execucao
        try {
            Files.createDirectories(outputPackagePath);
            Writer.write(outputFilePath, pathsCtx.getGeneratedClass());
            LOGGER.log(Levels.SUCCESS.getLevel(), "***File created in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
        }
        catch (IOException e) {
            Utils.logException(e);
        }
        finally {
        	// apos toda a etapa de processamento acima, podemos garantir que o caminho do arquivo de saida sera atribuido a variavel de caminho do arquivo fonte existente 
        	pathsCtx.setExistingPJavaGeneratedSourcePath(outputFilePath);
        }
    }
    
    public static <T> void write(Path pathToWrite, T content) {
    	 try {
    		StandardOpenOption[] options = {
    				StandardOpenOption.CREATE, 
    				StandardOpenOption.WRITE, 
    				StandardOpenOption.SYNC
    		};
    		
			Utils.getExecutor().submit(() -> {
				 try {
					 switch(content) {
					 	case String s -> {
					 		LOGGER.info( "Writing data...\n");
					 		Files.writeString(pathToWrite, s, options);
					 	}
					 	case byte[] b -> {
					 		LOGGER.info( "Writing byte data...\n");
					 		Files.write(pathToWrite, b);
					 	}
					 	default -> throw new IllegalArgumentException("Unexpected value: " + content);
					 }
			     }
				 catch (IOException | IllegalArgumentException e) {
					 Utils.logException(e);
				 }
			 }).get();
		 } catch (InterruptedException | ExecutionException e) {
			Utils.logException(e);
		 }
    }
    
    // deve preparar todos os dados necessarios para a escrita do json
    public static void writeJson() {
    	Gson gson = new Gson();
    	
    	CacheManager.getQueuedCacheFiles(true)
	            	.stream()
	            	.forEach(path -> {
	            		var model = InnerStaticClassModel.initInstance(path);
	            		var cacheModel = new CacheModel(model);
	            		
	            		OutterClassModel.computeClassModelToMap(model);
			            CacheManager.computeCacheModelToMap(path, cacheModel);
			            
			            write(Utils.resolveJsonFilePath(path), gson.toJson(cacheModel));
	            	});
    }
}