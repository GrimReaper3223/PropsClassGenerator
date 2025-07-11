package com.dsl.classgen.io.file_manager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.io.cache_manager.CacheManager;
import com.dsl.classgen.io.cache_manager.CacheModel;
import com.dsl.classgen.utils.Utils;
import com.google.gson.Gson;

public class Writer {
	
	private static final Logger LOGGER = LogManager.getLogger(Writer.class);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	private static PathsContext pathCtx = fwCtx.getPathsContextInstance();
	
    private Writer() {}

    public static void write() {
    	LOGGER.log(Level.INFO, "Writing data...\n");
        Path outputPackagePath = pathCtx.getOutputSourceDirPath();
        Path outputFilePath = pathCtx.getOutputSourceFilePath();
        
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
            	LOGGER.log(Level.WARN, "***File already exists in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
                return;
            }
            Writer.fileWriter(outputFilePath);
            LOGGER.log(Level.WARN, "***File created in: {} [Elapsed Time: {}ms]***\n", outputPackagePath, Utils.calculateElapsedTime());
        }
        catch (IOException | InterruptedException | ExecutionException e) {
            LOGGER.error(e);
            treatInterruptions(e);
        }
        finally {
        	// apos toda a etapa de processamento acima, podemos garantir que o caminho do arquivo de saida sera atribuido a variavel de caminho do arquivo fonte existente 
        	pathCtx.setExistingPJavaGeneratedSourcePath(outputFilePath);
        }
    }
    
    public static void write(String content) {
    	LOGGER.log(Level.INFO, "Writing data...\n");
    	Path outputFilePath = pathCtx.getExistingPJavaGeneratedSourcePath();
    	
    	try {
    		Files.deleteIfExists(outputFilePath);
    		Files.createFile(outputFilePath);
    		pathCtx.setGeneratedClass(content);
    		fileWriter(outputFilePath);
    	} catch (IOException | InterruptedException | ExecutionException e) {
    		LOGGER.error(e);
    		treatInterruptions(e);
    	}
    }

    // escreve o arquivo de classe gerada no sistema de arquivos
    private static final void fileWriter(Path outputFilePath) throws InterruptedException, ExecutionException {
        Utils.getExecutor().submit(() -> {
            try (OutputStream out = Files.newOutputStream(outputFilePath)){
                out.write(pathCtx.getGeneratedClass().getBytes());
            }
            catch (IOException e) {
                LOGGER.error(e);
            }
        }).get();
    }

    // escreve todos os jsons gerados no sistema de arquivos, no caminho definido como o diretorio base para arquivos de cache
    private static final void jsonWriter(CacheModel htm, Path jsonFilePath) throws InterruptedException, ExecutionException {
        Utils.getExecutor().submit(() -> {
            try (OutputStream out = Files.newOutputStream(jsonFilePath)){
                out.write(new Gson().toJson(htm).getBytes());
            }
            catch (IOException e) {
                LOGGER.error(e);
            }
        }).get();
    }

    // metodo helper do metodo jsonWritter que recebe um Map.Entry como parametro e o desembala
    private static final void jsonWriter(Map.Entry<Path, CacheModel> entry) throws InterruptedException, ExecutionException {
        Writer.jsonWriter(entry.getValue(), entry.getKey());
    }

    // deve preparar todos os dados necessarios para a escrita do json
    public static void writeJson() {
        if (flagsCtx.getIsSingleFile()) {
        	try {
	            Path jsonFilePath = Utils.resolveJsonFilePath(pathCtx.getPropertiesFileName());
	            CacheModel htm = new CacheModel(pathCtx.getInputPropertiesPath(), fwCtx.getProps());
	            CacheManager.computeElementToCacheModelMap(jsonFilePath, htm);
				Writer.jsonWriter(htm, jsonFilePath);
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error(e);
				treatInterruptions(e);
			}
            
        } else {
        	CacheManager.getAllCacheFilesToWrite()
		            	.stream()
		            	.map(path -> {
				            Reader.loadPropFile(path);
				            Path jsonFilePath = Utils.resolveJsonFilePath(pathCtx.getPropertiesFileName());
				            CacheModel htm = new CacheModel(path, fwCtx.getProps());
				            Map.Entry<Path, CacheModel> entryModel = Map.entry(jsonFilePath, htm);
				            CacheManager.computeElementToCacheModelMap(jsonFilePath, htm);
				            return entryModel;
		            	}).forEach(t -> {
							try {
								Writer.jsonWriter(t);
							} catch (InterruptedException | ExecutionException e) {
								LOGGER.error(e);
								treatInterruptions(e);
							}
						});
        }
    }
    
    private static void treatInterruptions(Exception e) {
    	if(e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
    		LOGGER.error("Thread is interrupted.", e);
        	Thread.currentThread().interrupt();
        }
    }
}

