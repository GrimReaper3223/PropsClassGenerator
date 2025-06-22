package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public class GeneratedStructureChecker {
	
    public static void checkGeneratedStructure() {
        try {
            Values.setIfDirStructureAlreadyGenerated(Utils.getExecutor().submit(() -> {
                try (Stream<Path> dirs = GeneratedStructureChecker.constructDirStreamFind()){
                    return dirs.filter(path -> path.toString().contains("generated"))
                    		   .findFirst()
                    		   .flatMap(path -> {
			                       Values.setPackageClass(Utils.extractPackageName(path.toString()));
			                       return GeneratedStructureChecker.isExistsJavaFile(path);
                    		   }).orElse(false);
                }
            }).get());
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void checkIfExistsCompiledClass() {
        Path fullCompilationPath = Values.getCompilationPath().resolve(Values.getOutputFilePath());
        Values.setIfExistsCompiledPJavaClass(Files.exists(fullCompilationPath));
    }

    private static Stream<Path> constructDirStreamFind() {
        Stream<Path> dirs = null;
        try {
            dirs = Files.find(Values.getOutputPackagePath(),
            		Short.MAX_VALUE, 
            		(path, _) -> Files.isDirectory(path));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    private static Optional<Boolean> isExistsJavaFile(Path path) {
        boolean isExistsJavaFile = false;
        try (Stream<Path> files = Files.list(path);){
            isExistsJavaFile = files.map(p -> {
                boolean val = p.getFileName().toString().equals(Values.getOutterClassName() + ".java");
                if (val) {
                    Values.setExistingPJavaGeneratedSourcePath(p);
                }
                return val;
            }).findFirst()
            .isPresent();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.of(isExistsJavaFile);
    }
}

