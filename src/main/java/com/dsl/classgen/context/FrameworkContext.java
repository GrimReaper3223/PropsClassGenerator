package com.dsl.classgen.context;

import java.io.IOException;
import java.util.Properties;

public class FrameworkContext {

	static final FrameworkContext INSTANCE = new FrameworkContext();
	
	final Properties props;							// objeto que vai armazenar as propriedades dos arquivos carregados
	
	final PathsContext pathsContextInstance;
	final FlagsContext flagsContextInstance;
	
    private long timeOperation;						// guarda o tempo do sistema no momento em que a geracao se inicia
	
	private FrameworkContext() {
		props = new Properties();
		
		flagsContextInstance = new FlagsContext();
		pathsContextInstance = new PathsContext(flagsContextInstance.getIsDebugMode());
		
		timeOperation = 0L;
	}
	
	/*
	 * get() instances
	 */
	public static FrameworkContext get() {
		return INSTANCE;
	}
	
	public FlagsContext getFlagsInstance() {
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
