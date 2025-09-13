package com.dsl.classgen.io;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

import com.dsl.classgen.utils.Utils;

public final class StructureChecker extends SupportProvider {

	private StructureChecker() {}

	public static void checkStructure() {
		LOGGER.info("Analyzing the file system...");
		CountDownLatch latch = new CountDownLatch(2);
		var outputSourceDirPath = pathsCtx.getOutputSourceDirPath();

		Utils.getExecutor().execute(() -> {
			try {
				Files.walkFileTree(pathsCtx.getOutputSourceDirPath(), new FileVisitorImpls.SourceFinderFV());
			} catch (IOException e) {
				Utils.handleException(e);
			} finally {
				latch.countDown();
			}
		});

		Utils.getExecutor().execute(() -> {
			try {
				Files.walkFileTree(pathsCtx.getOutputClassFilePath(), new FileVisitorImpls.CompilationFinderFV());
			} catch (IOException e) {
				Utils.handleException(e);
			} finally {
				latch.countDown();
			}
		});

		try {
			latch.await();
		} catch (InterruptedException e) {
			Utils.handleException(e);
		}

		if(!flagsCtx.hasDirStructureAlreadyGenerated()) {
	        pathsCtx.setOutputSourceDirPath(outputSourceDirPath.resolve(Utils.normalizePath(pathsCtx.getPackageClass(), ".", "/")));
	        pathsCtx.setOutputSourceFilePath(pathsCtx.getOutputSourceDirPath().resolve(pathsCtx.getOutterClassName() + ".java"));
		} else {
			pathsCtx.setOutputSourceDirPath(outputSourceDirPath);
		}
	}
}