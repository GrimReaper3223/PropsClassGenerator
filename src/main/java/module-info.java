module com.dsl.classgen {
	requires static org.junit.jupiter.api;
	
	requires org.apache.logging.log4j.core;
	requires org.apache.logging.log4j;
	requires com.google.gson;
	requires java.compiler;

	exports com.dsl.classgen;
	exports com.dsl.classgen.annotation;
	exports com.dsl.classgen.annotation.containers;
	
	exports com.dsl.classgen.io.file_manager to com.dsl.test.classgen;
	exports com.dsl.classgen.generator to com.dsl.test.classgen;
	exports com.dsl.classgen.annotation.processors to com.dsl.test.classgen;
	
	opens com.dsl.classgen.io.cache_manager to com.google.gson;
}