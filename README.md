# PropsClassGenerator
Generate .java class files from a properties file


# How to use:

- Download the project's .jar and place it in your project's classpath;
- Call **Generator.init()** to initialize the variable values. **init()** has three parameters:
	
> • **Path inPath**: The path of the .properties file (single generation), or of the directory where .properties files exist (multiple inner classes - *generation does not recurse into directories*);
	
> • **Path outPath**: The output directory where the class should be created. For everything to work correctly, use the src/main/java directory of your project;
	
> • **String packageClass**: The name of the package where you want the class to be generated. This package will also be explicitly defined in the class. **Note: '.generated' will be appended to the end of the package name, with 'generated' being a subpackage of the package you provided**;

> There is also an overload of this method that accepts Strings instead of Paths.

- After all the settings are done, call the **Generator.generate()** method;

#### **IMPORTANT:**
You must define a comment at the top of your .properties file that defines the format of the data held. <br>
Enter **# $javatype:@String** to generate final variables of type String.

~For now, only String type values ​​are supported.~

Support for new Java types was added in version 0.1:

- all primitive types;
- all wrapper types;

There are types that are not yet supported. Trying to add unsupported types may result in unexpected behavior.

Types must be added in the properties file, as mentioned above.

