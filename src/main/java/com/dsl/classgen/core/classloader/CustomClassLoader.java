package com.dsl.classgen.core.classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.dsl.classgen.utils.Utils;

public class CustomClassLoader extends ClassLoader {

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// convert from binary name format to file path
		if (name.contains(".") && !name.contains("/")) {
			// replace dots with file separators
			name = "target/classes/" + Utils.normalizePath(name, ".", "/").toString().concat(".class");
		}

		byte[] classData = loadClassData(name);
		if (classData == null || classData.length == 0) {
			throw new ClassNotFoundException("Class data for " + name + " not found");
		}
		// convert to binary name format
		return defineClass(pathFormatToBinaryNameFormat(name), classData, 0, classData.length);
	}

	private byte[] loadClassData(String classPath) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (var inputStream = Files.newInputStream(Path.of(classPath))) {
			if (inputStream != null) {
				int read;
				while ((read = inputStream.read()) != -1) {
					baos.write(read);
				}
			}
		} catch (Exception e) {
			Utils.handleException(e);
		}
		return baos.toByteArray();
	}

	private String pathFormatToBinaryNameFormat(@NonNull String value) {
		// assumes the path contains the file name and replaces file separators with
		// dots and removes the file extension
		Path filePath = Path.of(value);
		Path parentPath = filePath.subpath(2, filePath.getNameCount() - 1);

		String binaryName = Utils.normalizePath(parentPath, File.separator, ".").toString()
				.concat("." + Utils.getSafetyFileName(filePath, "").toString());
		return binaryName.replace(".class", "");
	}
}
