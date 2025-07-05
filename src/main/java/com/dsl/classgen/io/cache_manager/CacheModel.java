package com.dsl.classgen.io.cache_manager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class CacheModel implements Serializable {
	
    private static final long serialVersionUID = 1L;
    
    private transient Properties props;
    
    public String filePath;						// caminho do arquivo de propriedades
    public int fileHash;						// hash do arquivo de propriedades
    public Map<String, Integer> hashTableMap;	// pares chave-valor do arquivo de propriedades deste objeto

    public CacheModel() {}
    
    public CacheModel(Path filePath, Properties props) {
    	this.props = props;
        this.filePath = filePath.toString();
        this.fileHash = hashCode();
        this.hashTableMap = initPropertyMapGraph();
    }

    // inicializa o grafo dos dados do arquivo de propriedades dentro de hashTableMap
    public Map<String, Integer> initPropertyMapGraph() {
        return props.entrySet()
        			.stream()
        			.map(entry -> Map.entry(entry.getKey().toString(), Objects.hash(entry.getKey().toString(), entry.getValue())))
        			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // compara dois objetos HashTableModel pelos seus hashTableMaps e hash de arquivo
    @Override
    public boolean equals(Object obj) {
        CacheModel cm = (CacheModel) Objects.requireNonNull(obj);
        boolean result = cm.hashTableMap.entrySet().containsAll(hashTableMap.entrySet());
        return cm.fileHash == fileHash && result;
    }

    // faz o hash do arquivo de propriedades
    @Override
    public int hashCode() {
        FileTime creationTime = FileTime.fromMillis(0L);
        FileTime lastModifiedTime = FileTime.fromMillis(0L);
        long fileSize = 0L;
        
        try {
        	BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
            creationTime = attrs.creationTime();
            lastModifiedTime = attrs.lastModifiedTime();
            fileSize = attrs.size();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return Objects.hash(creationTime.toMillis(), lastModifiedTime.toMillis(), fileSize);
    }
}

