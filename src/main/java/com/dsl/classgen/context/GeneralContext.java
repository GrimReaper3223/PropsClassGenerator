package com.dsl.classgen.context;

import java.io.IOException;
import java.util.Properties;

public class GeneralContext {

	private static final GeneralContext INSTANCE = new GeneralContext();
	
	private final Properties props = new Properties();
	
	private final FlagsContext flagsContextInstance = new FlagsContext();
	private final PathsContext pathsContextInstance = new PathsContext(flagsContextInstance.getIsDebugMode());
	
    private long timeOperation;
	
	private GeneralContext() {}
	
	/*
	 * get() instances
	 */
	public static GeneralContext getInstance() {
		return INSTANCE;
	}
	
	public FlagsContext getFlagsContextInstance() {
		return flagsContextInstance;
	}
	
	public PathsContext getPathsContextInstance() {
		return pathsContextInstance;
	}
	
	/*
	 * get/set() others
	 */
	public long getTimeOperation() {
		return timeOperation;
	}
	
	public void setTimeOperation(long timeOperation) {
		this.timeOperation = timeOperation;
	}
	
	public Properties getProps() {
		return props;
	}
	
	public static IOException throwIOException() {
        return new IOException("""
    	        Error: The variable type identification was not found for creating the classes.
    	        Enter the variable type and try again.
    	        Preferably, place the identifier at the top of the file so that it can be analyzed faster.
    	        
    	        Ex.:
            		 # $javatype:@String
            		 
            		 ... rest of the .properties file...
            		 
            	# - Comment for the property file;
            	$javatype - Java type identifier;
            	: - Syntactic separator;
            	@ - Tells the service that after this identifier, the java type will be read for the variable;
            	String - The java type used in this example;
            """);
    }
}
