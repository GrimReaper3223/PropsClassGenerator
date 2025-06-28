module props_class_generator {
	requires static org.junit.jupiter.api;
	
	requires org.apache.logging.log4j.core;
	requires org.apache.logging.log4j;
	requires com.google.gson;
	requires java.compiler;

	exports com.dsl.classgen;
	exports com.dsl.classgen.annotations;
	exports com.dsl.classgen.annotations.containers;
	
	opens com.dsl.classgen.io.cache_system to com.google.gson;
}