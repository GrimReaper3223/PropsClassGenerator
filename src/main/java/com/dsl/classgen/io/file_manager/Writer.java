package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.dsl.classgen.io.SupportProvider;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public final class Writer extends SupportProvider {
	
    private Writer() {}

    public static void write() {
    	LOGGER.log(NOTICE, "Writing data...\n");
        Path outputPackagePath = pathsCtx.getOutputSourceDirPath();
        Path outputFilePath = pathsCtx.getOutputSourceFilePath();
        
        // se nao existir o pacote de saida, entao os diretorios e o arquivo gerado serao criados
        // se o pacote e saida existir, mas nao existir o arquivo de saida, entao somente ele sera gerado
        // caso contrario, o arquivo sera marcado como existente e o metodo retorna sua execucao
        try {
            if (Files.notExists(outputPackagePath)) {
                Files.createDirectories(outputPackagePath);
                Files.createFile(outputFilePath);
                
            } else if (Files.notExists(outputFilePath)) {
                Files.createFile(outputFilePath);
                
            } else {
            	LOGGER.warn("***File already exists in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
                return;
            }
            Writer.fileWriter(outputFilePath);
            LOGGER.log(SUCCESS, "***File created in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
        }
        catch (IOException | InterruptedException | ExecutionException e) {
            logException(e);
        }
        finally {
        	// apos toda a etapa de processamento acima, podemos garantir que o caminho do arquivo de saida sera atribuido a variavel de caminho do arquivo fonte existente 
        	pathsCtx.setExistingPJavaGeneratedSourcePath(outputFilePath);
        }
    }
    
    public static void write(String content) {
    	LOGGER.log(NOTICE, "Writing data...\n");
    	Path outputFilePath = pathsCtx.getExistingPJavaGeneratedSourcePath();
    	
    	try {
    		Files.deleteIfExists(outputFilePath);
    		Files.createFile(outputFilePath);
    		pathsCtx.setGeneratedClass(content);
    		fileWriter(outputFilePath);
    	} catch (IOException | InterruptedException | ExecutionException e) {
    		logException(e);
    	}
    }

    // escreve o arquivo de classe gerada no sistema de arquivos
    private static final void fileWriter(Path outputFilePath) throws InterruptedException, ExecutionException {
        Utils.getExecutor().submit(() -> {
            try (OutputStream out = Files.newOutputStream(outputFilePath)){
                out.write(pathsCtx.getGeneratedClass().getBytes());
            }
            catch (IOException e) {
            	logException(e);
            }
        }).get();
    }

    // escreve todos os jsons gerados no sistema de arquivos, no caminho definido como o diretorio base para arquivos de cache
    private static final void jsonWriter(CacheModel cm, Path jsonFilePath) throws InterruptedException, ExecutionException {
        Utils.getExecutor().submit(() -> {
            try (OutputStream out = Files.newOutputStream(jsonFilePath)){
                out.write(new Gson().toJson(cm).getBytes());
            }
            catch (IOException e) {
            	logException(e);
            }
        }).get();
    }

    // metodo helper do metodo jsonWritter que recebe um Map.Entry como parametro e o desembala
    private static final void jsonWriter(Map.Entry<Path, CacheModel> entry) {
        try {
			Writer.jsonWriter(entry.getValue(), entry.getKey());
		} catch (InterruptedException | ExecutionException e) {
			logException(e);
		}
    }

    // deve preparar todos os dados necessarios para a escrita do json
    public static void writeJson() {
    	CacheManager.getQueuedCacheFiles()
	            	.stream()
	            	.map(path -> {
			            Reader.loadPropFile(path);
			            Path jsonFilePath = Utils.resolveJsonFilePath(pathsCtx.getPropertiesFileName());
			            CacheModel cm = new CacheModel(path, generalCtx.getProps());
			            CacheManager.computeElementToCacheModelMap(jsonFilePath, cm);
			            return Map.entry(jsonFilePath, cm);
	            	}).forEach(Writer::jsonWriter);
    }

    private static void logException(Exception e) {
    	if(e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
    		LOGGER.error("Thread is interrupted.", e);
        	Thread.currentThread().interrupt();
        } else {
        	LOGGER.fatal(e);
        }
    }
}

