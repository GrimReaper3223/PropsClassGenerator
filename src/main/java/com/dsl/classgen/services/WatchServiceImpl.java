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

    // inicializa o servico de monitoramento de diretorio
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

    // faz o registro inicial dos caminhos ja processados
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
                return Map.ofEntries(WatchServiceImpl.verifyKey(key, path));
            }).forEach(keys::putAll);
        } else {
            Path inputPath = Files.isDirectory(Values.getInputPropertiesPath()) ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().getParent();
            WatchKey key = inputPath.register(watcher, EVENT_KIND_ARR);
            keys.putAll(Map.ofEntries(WatchServiceImpl.verifyKey(key, inputPath)));
        }
        System.out.println("Done.");
    }
    
    // registra um novo caminho sob demanda
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

    // faz a verificacao da chave do diretorio e imprime na tela o estado 
    // em que aquela chave vai ser registrada: se e registro ou atualizacao de chave de diretorio
    private static Map.Entry<WatchKey, Path> verifyKey(WatchKey key, Path path) {
        System.out.format("Checking %s...%n", path);
        Path mappedPath = keys.get(key);
        
        if (mappedPath == null) {
            System.out.format("Registering: %s%n", path);
            
        } else if (!path.equals(mappedPath)) {
            System.out.format("Updating: %s -> %s%n", mappedPath, path);
        }
        
        return Map.entry(key, path);
    }
    
    // processa o caminho recebido, verificando a chave e computando ela no mapa
    private static void processPropertyKeyFile(Path file) {
    	try {
    		var pair = verifyKey(file.register(watcher, EVENT_KIND_ARR), file);
    		keys.computeIfPresent(pair.getKey(), (_, _) -> pair.getValue());
    		Values.addFileToList(file);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
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
        System.out.println("Watching...");
        while (true) {
            WatchKey key;
            
            try {
                key = watcher.take();
            }
            catch (InterruptedException e) {
            	if(Thread.currentThread().isInterrupted()) {
            		System.err.println("\nWatcher thread is interrupted.");
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
            				.filter(event -> event.kind() != StandardWatchEventKinds.OVERFLOW)
            				.map(event -> {
					                WatchEvent<Path> eventPath = cast(event);
					                Path occurrence = path.resolve(eventPath.context());
					                return Map.entry(occurrence, eventPath.kind());
            				})
            				.filter(entry -> Utils.isPropertiesFile(entry.getKey()) || Files.isDirectory(entry.getKey()))
            				.forEach(entry -> {
            					System.out.format("%s: %s\n", entry.getValue().name(), entry.getKey());
            					Values.addChangedValueToMap(entry);
            				});
            				
            if (!key.reset()) {
            	keys.remove(key);
            	System.out.format("Key removed from KeyMap: %s%n", key);
            	if (keys.isEmpty()) {
            		System.out.println("\nThere are no keys remaining for processing. Ending Watcher...");
            		break;
            	}
            }
        }
    }
}

