package com.dsl.classgen.services;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import com.dsl.classgen.io.Values;

/**
 * Deve implementar a visualizacao de alteracao de arquivo
 * 
 * @since
 * @version
 * @author Deiv
 */
public class WatchServiceImpl {
	
	private static final Thread watchServiceThread = new Thread(WatchServiceImpl::processEvents);
	private static final Map<WatchKey, Path> keys = new HashMap<>();
	private static WatchService watcher;
	
	public static void initialize() {
		// permite a execucao da thread de observacao de diretorio em segundo plano
		if(!watchServiceThread.isAlive()) {
			watchServiceThread.setDaemon(false);
			try {
				watcher = FileSystems.getDefault().newWatchService();
				register();
				watchServiceThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void register() throws IOException {
		if(Values.isRecursive()) {
			Values.getDirList().stream().map(path -> {
				WatchKey key = null;
				try {
					key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return verifyKey(key, path);
			}).forEach(keys::putAll);
			
		} else {
			Path inputPath = Files.isDirectory(Values.getInputPropertiesPath()) ? Values.getInputPropertiesPath() : Values.getInputPropertiesPath().getParent();
			WatchKey key = inputPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			keys.putAll(verifyKey(key, inputPath));
		}
		System.out.println("Done.");
	}

	private static Map<WatchKey, Path> verifyKey(WatchKey key, Path path) {
		System.out.format("Checking %s...%n", path);
		Path mappedPath = keys.get(key);

		if (mappedPath == null) {
			System.out.format("Register: %s%n", path);
		} else {
			if (!path.equals(mappedPath)) {
				System.out.format("Update: %s -> %s%n", mappedPath, path);
			}
		}
		
		return Map.of(key, path);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}
	
	private static void processEvents() {
		System.out.println("Watching...");
		while(true) {
			WatchKey key;
			
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				break;
			}
			
			Path path = keys.get(key);
			if(path == null) {
				System.err.println("WatchKey not recognized.");
				continue;
			}
			
			key.pollEvents().stream()
					.map(event -> Map.entry(event, event.kind()))
					.filter(entry -> entry.getValue() != OVERFLOW)
					.map(entry -> {
						WatchEvent<Path> event = cast(entry.getKey());
						Path occurrence = path.resolve(event.context());
						System.out.format("%s: %s\n", event.kind().name(), occurrence);
						return Map.entry(occurrence, entry.getValue());
					})
					.forEach(Values::addChangedValueToMap);
			
			if(!key.reset()) {
				keys.remove(key);
				if(keys.isEmpty()) {
					break;
				}
			}
		}
	}
}