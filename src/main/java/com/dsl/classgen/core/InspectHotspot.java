package com.dsl.classgen.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.dsl.classgen.utils.Utils;

public class InspectHotspot {

	private String getExecutionModule() {
		ModuleFinder mf = ModuleFinder.of(Path.of("target/classes"));
		return mf.findAll().stream().findFirst().orElseThrow().descriptor().name();
	}

	private Stream<String> filterHotspotsByModuleRef() {
		Stream<String> listStream = Stream.empty();
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "jps -l");
			Process p = pb.start();
			try (BufferedReader reader = p.inputReader()) {
				listStream = reader.lines().filter(line -> line.contains(getExecutionModule())).toList().stream();
			}
		} catch (IOException e) {
			Utils.handleException(e);
		}
		return listStream;
	}

	private Stream<String> processesPidStream() {
		return filterHotspotsByModuleRef().map(process -> {
			int pid = Integer.parseInt(process.split("\\W")[0]);
			return String.format("jmap -histo:live %d | grep \"com.dsl.classgen\" -m1 ", pid);
		}).toList().stream();
	}

	public long lookupForHotspotModuleExecution() {
		return processesPidStream().map(cmd -> {
			int exitCode = 0;
			try {
				exitCode = new ProcessBuilder("/bin/bash", "-c", cmd).start().waitFor();
			} catch (InterruptedException | IOException e) {
				Utils.handleException(e);
			}
			return exitCode;
		}).filter(i -> i == 0).count();
	}
}
