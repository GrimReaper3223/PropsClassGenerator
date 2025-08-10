module com.dsl.classgen {
	requires static org.junit.jupiter.api;

	requires transitive org.apache.logging.log4j.core;
	requires transitive org.apache.logging.log4j;
	requires transitive com.google.gson;
	requires transitive com.github.javaparser.core;
	requires java.compiler;

	exports com.dsl.classgen;
	exports com.dsl.classgen.annotation;
	exports com.dsl.classgen.annotation.containers;
	exports com.dsl.classgen.io.synchronizer to com.dsl.classgen.io.cache_manager;
	exports com.dsl.classgen.models;
	exports com.dsl.classgen.models.model_mapper;

	// for test package
	exports com.dsl.classgen.io.file_manager to com.dsl.test.classgen;
	exports com.dsl.classgen.context to com.dsl.test.classgen;
	exports com.dsl.classgen.generator to com.dsl.test.classgen;
	exports com.dsl.classgen.annotation.processors to com.dsl.test.classgen;
	exports com.dsl.classgen.utils to com.dsl.test.classgen;

	opens com.dsl.classgen.io to com.google.gson;
}