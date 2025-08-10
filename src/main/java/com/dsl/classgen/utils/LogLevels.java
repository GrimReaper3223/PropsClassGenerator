package com.dsl.classgen.utils;

import org.apache.logging.log4j.Level;

/**
 * The Enum LogLevels.
 */
public enum LogLevels {

	CACHE("CACHE"),
	NOTICE("NOTICE"),
	SUCCESS("SUCCESS");

	private final Level level;

	/**
	 * Instantiates a new log levels.
	 *
	 * @param level the level string representation
	 */
	LogLevels(String level) {
		this.level = Level.getLevel(level);
	}

	/**
	 * @return the level of this enum instance
	 */
	public Level getLevel() {
		return level;
	}
}
