module props_class_generator {
	requires static org.junit.jupiter.api;
	requires com.google.gson;
	
	exports com.dsl.classgen;
	exports com.dsl.classgen.annotations;
	
	opens com.dsl.classgen.io to com.google.gson;
}