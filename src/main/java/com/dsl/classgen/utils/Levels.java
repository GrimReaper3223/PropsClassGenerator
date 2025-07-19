package com.dsl.classgen.utils;

import org.apache.logging.log4j.Level;

public enum Levels {

	CACHE("CACHE"),
	NOTICE("NOTICE"),
	SUCCESS("SUCCESS");
	
	private final Level level;
	
	private Levels(String level) {
		this.level = Level.getLevel(level);
	}
	
	public Level getLevel() {
		return level;
	}
}
