package com.dsl.test.classgen.classloader;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dsl.classgen.utils.Utils;
import com.dsl.test.classgen.HarnessTestTools;

class CustomClassLoaderTest extends ClassLoader implements HarnessTestTools {

	@Test
	void testFindClass() throws Exception {
		Assertions.assertNotNull(findClass(classPath.subpath(2, classPath.getNameCount()).toString()));
		System.out.println("Class loaded successfully: " + classPath);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classData = loadClassData(name);
		if (classData == null || classData.length == 0) {
			throw new ClassNotFoundException("Class data for " + name + " not found");
		}
		String newName = classPath.subpath(2, classPath.getNameCount() - 1).toString().replace("/", ".").concat(".P");
		return defineClass(newName, classData, 0, classData.length);
	}
	
	private byte[] loadClassData(String name) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (var inputStream = getClass().getClassLoader().getResourceAsStream(name)) {
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
}
