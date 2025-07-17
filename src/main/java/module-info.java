module propsClassGenerator {
	requires static org.junit.jupiter.api;
	
	requires org.apache.logging.log4j.core;
	requires org.apache.logging.log4j;
	requires com.google.gson;
	requires java.compiler;

	exports com.dsl.classgen;
	exports com.dsl.classgen.annotation;
	exports com.dsl.classgen.annotation.containers;
	
	opens com.dsl.classgen.io.cache_manager to com.google.gson;
}