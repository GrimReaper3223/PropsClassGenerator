package com.dsl.classgen.service;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dsl.classgen.context.FlagsContext;
import com.dsl.classgen.context.GeneralContext;
import com.dsl.classgen.context.PathsContext;
import com.dsl.classgen.utils.Utils;

public class WatchServiceImpl {
	private static final Logger LOGGER = LogManager.getLogger(WatchServiceImpl.class);

	private static GeneralContext generalCtx = GeneralContext.get();
	private static PathsContext pathsCtx = generalCtx.getPathsContextInstance();
	private static FlagsContext flagsCtx = generalCtx.getFlagsInstance();
	
    private static final Thread watchServiceThread = new Thread(WatchServiceImpl::processEvents);
    private static WatchService watcher;
    
    private static final Map<WatchKey, Path> keys = new HashMap<>();
    private static final Kind<?>[] EVENT_KIND_ARR = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}; 

    private WatchServiceImpl() {}
    
    static {
    	try {
            watcher = FileSystems.getDefault().newWatchService();
        }
        catch (IOException e) {
        	LOGGER.error(e);
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
            	LOGGER.error(e);
            }
        }
    }

    private static void initialRegistration() throws IOException {
        if (flagsCtx.getIsRecursive()) {
        	pathsCtx.getDirList().stream().map(path -> {
                WatchKey key = null;
                try {
                    key = path.register(watcher, EVENT_KIND_ARR);
                }
                catch (IOException e) {
                	LOGGER.error(e);
                }
                return Map.ofEntries(WatchServiceImpl.verifyKey(key, path));
            }).forEach(keys::putAll);
        } else {
            Path inputPath = Files.isDirectory(pathsCtx.getInputPropertiesPath()) ? pathsCtx.getInputPropertiesPath() : pathsCtx.getInputPropertiesPath().getParent();
            WatchKey key = inputPath.register(watcher, EVENT_KIND_ARR);
            keys.putAll(Map.ofEntries(WatchServiceImpl.verifyKey(key, inputPath)));
        }
        LOGGER.info("Done\n");
    }
    
    private static Map.Entry<WatchKey, Path> verifyKey(WatchKey key, Path path) {
    	LOGGER.info("Checking {}...", path);
        Path mappedPath = keys.get(key);
        
        if (mappedPath == null) {
        	LOGGER.info("Registering: {}...", path);
            
        } else if (!path.equals(mappedPath)) {
        	LOGGER.info("Updating: {} -> {}...", mappedPath, path);
        }
        
        return Map.entry(key, path);
    }
    
    public static void analysePropertyDir(Path dir) throws IOException {
		var pair = verifyKey(dir.register(watcher, EVENT_KIND_ARR), dir);
		if(keys.computeIfPresent(pair.getKey(), (_, _) -> pair.getValue()) == null) {
			keys.put(pair.getKey(), pair.getValue());
		}
    }

    @SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private static void processEvents() {
    	LOGGER.warn("\nWatching...");
        while (true) {
            WatchKey key = null;
            
            try {
                key = watcher.take();
            }
            catch (InterruptedException _) {
            	if(Thread.currentThread().isInterrupted()) {
            		LOGGER.error("Watcher thread is interrupted");
            		Thread.currentThread().interrupt();
            	}
            }
            
            if (!keys.containsKey(key)) {
            	LOGGER.warn("WatchKey not recognized.");
            	continue;
            }
            
            processStream(key);
            				
            if (!key.reset()) {
            	keys.remove(key);
            	
            	if (keys.isEmpty()) {
            		LOGGER.warn("There are no keys remaining for processing. Ending Watcher...");
            		break;
            	}
            }
        }
    }
    
    private static void processStream(WatchKey key) {
    	key.pollEvents()
    		.stream()
    		.filter(event -> event.kind() != StandardWatchEventKinds.OVERFLOW)
    		.map(event -> {
    			WatchEvent<Path> eventPath = cast(event);
    			Path occurrence = keys.get(key).resolve(eventPath.context());
    			return Map.entry(occurrence, eventPath.kind());
		  	 })
		  	 .filter(entry -> Utils.isPropertiesFile(entry.getKey()) || Files.isDirectory(entry.getKey()))
		  	 .forEach(entry -> {
					LOGGER.info("{}: {}", entry.getValue().name(), entry.getKey());
					pathsCtx.queueChangedFileEntry(entry);
		   	});
    }
    
    public static boolean isWatchServiceThreadAlive() {
    	return watchServiceThread.isAlive();
    }
}

