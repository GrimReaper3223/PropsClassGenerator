package com.dsl.classgen.services;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.dsl.classgen.io.Values;
import com.dsl.classgen.utils.Utils;

public class WatchServiceImpl {
    private static final Thread watchServiceThread = new Thread(WatchServiceImpl::processEvents);
    private static final Map<WatchKey, Path> keys = new HashMap<>();
    private static WatchService watcher;
    
    private static final Kind<?>[] EVENT_KIND_ARR = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}; 

    public static void initialize() {
        if (!watchServiceThread.isAlive()) {
            watchServiceThread.setDaemon(false);
            try {
                watcher = FileSystems.getDefault().newWatchService();
                initialRegistration();
                watchServiceThread.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initialRegistration() throws IOException {
        if (Values.isRecursive()) {
            Values.getDirList().stream().map(path -> {
                WatchKey key = null;
                try {
                    key = path.register(watcher, EVENT_KIND_ARR);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return WatchServiceImpl.verifyKey(key, path);
            }).forEach(keys::putAll);
        } else {
            Path inputPath = Files.isDirectory(Values.getInputPropertiesPath()) ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().getParent();
            WatchKey key = inputPath.register(watcher, EVENT_KIND_ARR);
            keys.putAll(WatchServiceImpl.verifyKey(key, inputPath));
        }
        System.out.println("Done.");
    }
    
    public static void registerNewPath(Path path) {
    	switch(path) {
    		case Path dir when Files.isDirectory(path) -> {
    			try {
	    			if (Values.isRecursive()) {
    					Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
    						@Override
    						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    							if(Utils.isPropertiesFile(file)) {
    								processPropertyKeyFile(file);
    							}
    							return FileVisitResult.CONTINUE;
    						}
    					});
	    			} else {
	    				try(Stream<Path> files = Files.list(dir)) {
	    					files.filter(Files::isRegularFile)
	    						 .forEach(WatchServiceImpl::processPropertyKeyFile);
	    				}
	    			}
    			}
	    		catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    		case Path file when Files.isRegularFile(path) -> {
    			processPropertyKeyFile(file);
    		}
    		
    		default -> throw new IllegalArgumentException("Unexpected value: " + path);
    	}
    }

    private static Map<WatchKey, Path> verifyKey(WatchKey key, Path path) {
        System.out.format("Checking %s...%n", path);
        Path mappedPath = keys.get(key);
        
        if (mappedPath == null) {
            System.out.format("Registering: %s%n", path);
            
        } else if (!path.equals(mappedPath)) {
            System.out.format("Updating: %s -> %s%n", mappedPath, path);
        }
        
        return Map.of(key, path);
    }
    
    private static Map.Entry<WatchKey, Path> verifyKey(WatchKey key, Path path, Object... voidArg) {
    	Map.Entry<WatchKey, Path> entry = null;
    	for(var entrySet : verifyKey(key, path).entrySet()) {
    		entry = Map.entry(entrySet.getKey(), entrySet.getValue());
    	}
    	return entry;
    }
    
    private static void processPropertyKeyFile(Path file) {
    	try {
    		var pair = verifyKey(file.register(watcher, EVENT_KIND_ARR), file, "");
    		keys.computeIfPresent(pair.getKey(), (_, _) -> pair.getValue());
    		Values.addFileToList(file);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    @SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event.context();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static void processEvents() {
        System.out.println("Watching...");
        while (true) {
            WatchKey key;
            
            try {
                key = watcher.take();
            }
            catch (InterruptedException e) {
            	if(Thread.currentThread().isInterrupted()) {
            		Thread.currentThread().interrupt();
            	}
                break;
            }
            
            Path path = keys.get(key);
            if (path == null) {
                System.err.println("WatchKey not recognized.");
                continue;
            }
            
            key.pollEvents().stream()
            				.map(event -> Map.entry(event, event.kind()))
            				.filter(entry -> entry.getValue() != StandardWatchEventKinds.OVERFLOW)
            				.map(entry -> {
					                WatchEvent<Path> event = WatchServiceImpl.cast(entry.getKey());
					                Path occurrence = path.resolve(event.context());
					                System.out.format("%s: %s\n", event.kind().name(), occurrence);
					                return Map.entry(occurrence, entry.getValue());
            				}).forEach(Values::addChangedValueToMap);
            				
            if (key.reset()) {
            	keys.remove(key);
            	if (keys.isEmpty()) {
            		break;
            	}
            }
        }
    }
}

