package com.dsl.classgen.utils;

import static com.dsl.classgen.io.Values.getEndTimeOperation;
import static com.dsl.classgen.io.Values.getStartTimeOperation;
import static com.dsl.classgen.io.Values.setEndTimeOperation;
import static com.dsl.classgen.io.Values.setStartTimeOperation;

public class Utils {

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
	
