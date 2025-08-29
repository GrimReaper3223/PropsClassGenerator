# PropsClassGenerator
Generate .java class files from a properties file

[![Java CI with Maven](https://github.com/GrimReaper3223/PropsClassGenerator/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/GrimReaper3223/PropsClassGenerator/actions/workflows/maven.yml)

# How to use:

- Download the project's .jar and place it in your project's classpath;
- Call **Generator.init()** to initialize the variable values. **init()** has three parameters:
	
> • **Path inputPath**: The path of the .properties file (single generation), or of the directory where .properties files exist (multiple inner classes - *generation does not recurse into directories*);
	
> • **String packageClass**: The name of the package where you want the class to be generated. This package will also be explicitly defined in the class. **Note: '.generated' will be appended to the end of the package name, with 'generated' being a subpackage of the package you provided**;

> • **boolean isRecursive**: Flag indicating whether the search for .properties files should be recursive in the directories of the given path. This has no effect if the path passed is of a file, rather than a directory;

> There is also an overload of this method that accepts Strings instead of Paths.

- After all the settings are done, call the **Generator.generate()** method;

- Alternatively, you can use the project as a maven dependency:

```
	<dependency>
		<groupId>io.github.grimreaper3223</groupId>
		<artifactId>PropsClassGenerator</artifactId>
		<version>1.0</version>
	</dependency>
```

# How it works:

In a simplified way, when entering the path to the properties file or directory containing the properties files, the package where the source code will be saved and the recursion option, the generation occurs as follows:

##### If it's the first generation:
- The entire directory structure is generated;
- The properties files are read and loaded into memory, initializing the cache models and hierarchical models of static inner classes and their internal fields;
- The JavaParser then generates all dependencies for the source code and stores everything in a **generatedClass** context variable;
- The writing methods access the **generatedClass** context variable and write the data to a Java source file;
- After writing, a compilation call to the JavaCompiler compiles all the source code at once;
- Finally, two threads are initialized to monitor the file system and process its events:

> • **Watch Service Thread**: Monitors the parent directory of a properties file (if only one file is analyzed), the entire directory of a properties file but not its subdirectories (if a directory is analyzed but not recursively), or all subdirectories of the analyzed directory plus the analyzed directory (if a directory is analyzed recursively). When a change is detected, the file path is inserted into a file stack for processing with the mapped event type;

> • **File Events Processor Thread**: Performs active processing of changed resources. This thread waits indefinitely for new files in the synchronous file stack for processing. When a new file is detected in the file stack for processing, this thread obtains the file and processes it, continuing this process until its counterpart, the **Watch Service Thread**, has no more files to offer to the synchronous file processing stack;

- From here on, any changes made to any of the mapped files are automatically synchronized with the source code and the compiled code at runtime. Source code synchronization is done with JavaParser, and binary code synchronization is done through an implementation of the Java ClassFile API;

#### If there is already an existing generation:
- The structure is no longer generated or regenerated, but rather analyzed;
- Some data passed by the developer to the framework through the **init()** method may no longer have any effect. For example, if a previous generation already exists, the framework does not read the **inputPath** variable or the **packageClass** variable, but it can still analyze the **isRecursive** variable in future reads;
- Since a generation already exists, a cache must also exist. This cache is loaded into memory along with the chunks of the class data model hierarchies;
- The framework deeply compares each chunk with each corresponding cache in a **resynchronization** task, searching for changes made while the framework was not running. These changes are filtered and processed, initiating the resynchronization task of the source code and compiled code; - If there are no discrepancies, the framework simply continues its execution;
- Then the change monitoring and event processing threads are started and execution continues analyzing changes in the mapped files;

#### Real-time changes

To take advantage of this dynamism, run the framework in your main project and keep the project thread running.

The framework contains two non-daemon threads:
- **Watch Service Thread**
- **File Events Processor Thread**

The first thread delegates changed files to the second thread, which only runs while the first thread is running.

While the framework is running, any change (modification, insertion or deletion) to the mapped property files will result in data being synchronized.

<hr>

#### **IMPORTANT:**
You must define a comment at the top of your .properties file that defines the format of the data held. <p>
Examples:

| Type def | Example in the source |
| ----------- | ----------- |
| **# $javatype:@String** | public static final **String** ... = "data"; |
| **# $javatype:@int** | public static final **int** ... = 123; |
| **# $javatype:@Character** | public static final **Character** ... = 'a'; |

Types such as <ins>**float**</ins>, <ins>**double**</ins>, <ins>**long**</ins>, and their wrapper variants must contain the type definition character  <ins>**f or F**</ins>, <ins>**d or D**</ins> or <ins>**l or L**</ins> next to the data. **The framework does not insert these identifiers.** This may be supported when new data types are integrated.

I also recommend defining the **'exports'** directive in your *module-info.java* file for the generated package containing the mapped properties.

If this isn't done, future uses of the framework may experience errors due to the generated package not being exported (this is due to an unresolvable bug, where I should have inserted the **'exports'** clause in the *module-info.java* file. However, as I found this to be too invasive, I decided to let the developer handle this).

In the future, with the builder pattern, I will include an option that you can use if you want the **'exports'** clause to be inserted automatically after the files are generated.

<hr>

## **CHANGELOG:**

### v1.0 [First Production Release]:

- Improved relational data mapping. The framework now uses the chunked loading model;
- Improved structure checkers;
- Some utility methods have been added, others have been improved, and others have been removed;
- The source code moification method has been changed. Previously, a model using Strings and StringBuilders was used; now the JavaParser has been implemented, using the AST (Abstract Syntax Tree) model;
- Resynchronization of mapped data with changes made while the framework was not running has been added;
- Improvements to annotation processors and class generators;
- Changes to the way compiled classes are loaded. Previously, the class was loaded by the platform's ClassLoader; now a custom ClassLoader implementation has been created to load compiled classes without restarting the application;
- Support for custom context variables for better data utilization;
- Complete implementation for creating, removing, or modifying static inner classes and their fields in compiled code using the ClassFile API;
- Java version 24 is required for use. This version provides improved ClassFile API methods;
- Implementation of the producer-consumer model between the thread that checks for changes in mapped file directories and the thread that processes these changes and synchronizes the changes;
- More refined use of the Log4j2 API;
- Optimized module dependencies in the module-info.java file;
- Optimized Maven dependencies;
- Optimized file reading/writing;
- General optimizations were made, such as code refactorings, elimination of redundancies, and use of more efficient bit-scale data types;
- Many bugs were fixed.

Some important implementations, such as checking for errors in class files, detecting the pre-existence of a variable before adding it to the source code and compiled code, and additional performance improvements, have not yet been made.
The supported data types remain the same (known Java primitive types and their wrappers). Custom types have not yet been tested, with the exception of the Class<?> type, for example.

#### Known issues in this version
- **No action occurs when deleting a properties file, added after loading the already known classes, while the framework is running.** This may be due to a lack of bytecode updates. No exception is thrown when this happens. The data simply isn't updated.
- **Possible resource overload.** I haven't done the profiling yet, but I suspect the properties files are read twice in a single operation in different phases.

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

