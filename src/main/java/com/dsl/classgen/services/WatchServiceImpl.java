package com.dsl.classgen.services;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.FrameworkContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.utils.Utils;

public class WatchServiceImpl {
	private static final Logger LOGGER = LogManager.getLogger(WatchServiceImpl.class);
	
	private static FrameworkContext fwCtx = FrameworkContext.get();
	private static PathsContext pathsCtx = fwCtx.getPathsContextInstance();
	private static FlagsContext flagsCtx = fwCtx.getFlagsInstance();
	
    private static final Thread watchServiceThread = new Thread(WatchServiceImpl::processEvents);
    private static final Map<WatchKey, Path> keys = new HashMap<>();
    private static WatchService watcher;
    
    private static final Kind<?>[] EVENT_KIND_ARR = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}; 

    private WatchServiceImpl() {}
    
    static {
    	try {
            watcher = FileSystems.getDefault().newWatchService();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // inicializa o servico de monitoramento de diretorio
    public static void initialize() {
        if (!watchServiceThread.isAlive()) {
            watchServiceThread.setDaemon(false);
            watchServiceThread.setName("Watch Service - Thread");
            try {
                initialRegistration();
                watchServiceThread.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // faz o registro inicial dos caminhos ja processados
    private static void initialRegistration() throws IOException {
        if (flagsCtx.getIsRecursive()) {
        	pathsCtx.getDirList().stream().map(path -> {
                WatchKey key = null;
                try {
                    key = path.register(watcher, EVENT_KIND_ARR);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return Map.ofEntries(WatchServiceImpl.verifyKey(key, path));
            }).forEach(keys::putAll);
        } else {
            Path inputPath = Files.isDirectory(pathsCtx.getInputPropertiesPath()) ? pathsCtx.getInputPropertiesPath() : pathsCtx.getInputPropertiesPath().getParent();
            WatchKey key = inputPath.register(watcher, EVENT_KIND_ARR);
            keys.putAll(Map.ofEntries(WatchServiceImpl.verifyKey(key, inputPath)));
        }
        LOGGER.log(Level.INFO, "Done\n");
    }
    
    // faz a verificacao da chave do diretorio e imprime na tela o estado 
    // em que aquela chave vai ser registrada: se e registro ou atualizacao de chave de diretorio
    private static Map.Entry<WatchKey, Path> verifyKey(WatchKey key, Path path) {
    	LOGGER.log(Level.INFO, "Checking {}...", path);
        Path mappedPath = keys.get(key);
        
        if (mappedPath == null) {
        	LOGGER.log(Level.INFO, "Registering: {}...", path);
            
        } else if (!path.equals(mappedPath)) {
        	LOGGER.log(Level.INFO, "Updating: {} -> {}...", mappedPath, path);
        }
        
        return Map.entry(key, path);
    }
    
    // processa o caminho recebido, verificando a chave e computando ela no mapa
    public static void analysePropertyDirKeyPath(Path dir) throws IOException {
		var pair = verifyKey(dir.register(watcher, EVENT_KIND_ARR), dir);
		if(keys.computeIfPresent(pair.getKey(), (_, _) -> pair.getValue()) == null) {
			keys.put(pair.getKey(), pair.getValue());
		}
    }

    // casting utilitario
    @SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static void processEvents() {
    	LOGGER.log(Level.WARN, "\nWatching...");
        while (true) {
            WatchKey key = null;
            
            try {
                key = watcher.take();
            }
            catch (InterruptedException _) {
            	if(Thread.currentThread().isInterrupted()) {
            		LOGGER.log(Level.ERROR, "Watcher thread is interrupted");
            		Thread.currentThread().interrupt();
            	}
            }
            
            Path path = keys.get(key);
            if (path == null) {
            	LOGGER.log(Level.WARN, "WatchKey not recognized.");
            	continue;
            }
            
            key.pollEvents().stream()
            				.filter(event -> event.kind() != StandardWatchEventKinds.OVERFLOW)
            				.map(event -> {
					                WatchEvent<Path> eventPath = cast(event);
					                Path occurrence = path.resolve(eventPath.context());
					                return Map.entry(occurrence, eventPath.kind());
            				})
            				.filter(entry -> Utils.isPropertiesFile(entry.getKey()) || Files.isDirectory(entry.getKey()))
            				.forEach(entry -> {
            					LOGGER.log(Level.INFO, "{}: {}", entry.getValue().name(), entry.getKey());
            					pathsCtx.addChangedEntryToQueue(entry);
            				});
            				
            if (!key.reset()) {
            	keys.remove(key);
            	
            	LOGGER.log(Level.INFO, "Key removed from KeyMap: {}", path);
            	if (keys.isEmpty()) {
            		LOGGER.warn("There are no keys remaining for processing. Ending Watcher...");
            		break;
            	}
            }
        }
    }
    
    public static boolean isWatchServiceThreadAlive() {
    	return watchServiceThread.isAlive();
    }
}

