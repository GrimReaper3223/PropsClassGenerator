# PropsClassGenerator
Generate .java class files from a properties file

[![Java CI with Maven](https://github.com/GrimReaper3223/PropsClassGenerator/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/GrimReaper3223/PropsClassGenerator/actions/workflows/maven.yml)

# How to use:

- Download the project's .jar and place it in your project's classpath;
- Call **Generator.init()** to initialize the variable values. **init()** has three parameters:
	
> • **Path inputPath**: The path of the .properties file (single generation), or of the directory where .properties files exist (multiple inner classes - *generation does not recurse into directories*);
	
> • **Path outputPath**: The output directory where the class should be created. For everything to work correctly, use the src/main/java directory of your project;
	
> • **String packageClass**: The name of the package where you want the class to be generated. This package will also be explicitly defined in the class. **Note: '.generated' will be appended to the end of the package name, with 'generated' being a subpackage of the package you provided**;

> • **boolean isRecursive**: Flag indicating whether the search for .properties files should be recursive in the directories of the given path. This has no effect if the path passed is of a file, rather than a directory;

> There is also an overload of this method that accepts Strings instead of Paths.

- After all the settings are done, call the **Generator.generate()** method;

#### **IMPORTANT:**
You must define a comment at the top of your .properties file that defines the format of the data held. <br>
Enter **# $javatype:@String** to generate final variables of type String.

## **CHANGELOG:**

### v0.2.4-R1:

**COMPILED STATE RECOVERY VERSION**

This version had to be decompiled from a backup because one of the commits was not done correctly and the entire original structure was corrupted.

It is recommended to use this version instead of version 0.2.4, as this version was recompiled with possible errors corrected from the previous version that caused the corruption in the framework.

### v0.2.4:
- Added JSON cache system;
- Added structure checkers (checks if the directory structure exists, if P.java exists, if there is already a compilation, etc.);
- Added more utility methods for formatting and parsing;
- Added libs directory containing libraries usable by the framework as resources. These libraries need to exist as resources for the successful execution of the .jar;
- Major improvements were made in several areas of the code (Cache system, Generator, Annotations, code in general, etc.);
- Major code refactoring;
- Large number of bugs fixed

### v0.2.3:
- Added support for virtual threads in I/O operations;
- Added metadata variables to annotations;
- Added support for monitoring directories containing property files with WatchService;
- Bug fixes;

### v0.2.2:

- Refactoring of all packages and classes;
- Improvements in separation of responsibilities;
- Improvements in module isolation, exporting only the generator class and abstracting the backend;
- Checker framework removal [NOT USED];

### v0.2.1:

- Fixed bug when analyzing base directory without recursion;

### v0.2:

- Add support for recursive checking. If you want .properties files to be searched in subdirectories, set the variable to **true** in the **init()** method;
- Added log in **System.out.println()** informing the developer of the entire process that took place;

### v0.1:

Support for new Java types was added in version 0.1:

- all primitive types;
- all wrapper types;

There are types that are not yet supported. Trying to add unsupported types may result in unexpected behavior.

Types must be added in the properties file, as mentioned above.

