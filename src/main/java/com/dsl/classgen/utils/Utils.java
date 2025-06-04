package com.dsl.classgen.utils;

import static com.dsl.classgen.io.Values.getEndTimeOperation;
import static com.dsl.classgen.io.Values.getStartTimeOperation;
import static com.dsl.classgen.io.Values.setEndTimeOperation;
import static com.dsl.classgen.io.Values.setStartTimeOperation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

	// executa threads virtuais para operacoes de leitura / escrita de arquivos
	private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	
	public static ExecutorService getExecutor() {
		return executor;
	}
	
	public static long calculateElapsedTime() {
		if(getStartTimeOperation() == 0L) {
			setStartTimeOperation(System.currentTimeMillis());
			return 0L;
		}
		
		if(getEndTimeOperation() == 0L) {
			setEndTimeOperation(System.currentTimeMillis());
		}
		
		return getEndTimeOperation() - getStartTimeOperation();
	}
}
	
