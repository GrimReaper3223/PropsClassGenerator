package com.dsl.classgen.models;

public interface Hints {

	String startHint();
	String endHint();
	
	public default String startAndEndHint() {
		return String.format("%s@%s", startHint(), endHint());
	}
}
