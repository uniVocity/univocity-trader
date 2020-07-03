package com.univocity.trader.chart.dynamic.code;

import javax.tools.*;
import java.io.*;
import java.util.*;

public class MemoryJavaCompiler {
	private final JavaCompiler compiler;
	private final StandardJavaFileManager fileManager;

	public MemoryJavaCompiler() {
		compiler = ToolProvider.getSystemJavaCompiler();
		fileManager = compiler.getStandardFileManager(null, null, null);
	}

	public <T> T compileString(final String className, final String source, Writer errorOutput) throws ClassNotFoundException {
		try {
			final Map<String, byte[]> classBytes = compileFile(className + ".java", source, errorOutput);
			final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
			final Class clazz = classLoader.loadClass(className);

			Object instance = clazz.getConstructor().newInstance();
			return (T) instance;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Map<String, byte[]> compileFile(String fileName, String source, Writer err) {
		MemoryJavaFileManager fileManager = new MemoryJavaFileManager(this.fileManager);
		JavaFileObject toCompile = MemoryJavaFileManager.makeStringSource(fileName, source);
		return compile(toCompile, fileManager, err);
	}

	private Map<String, byte[]> compile(JavaFileObject toCompile, final MemoryJavaFileManager fileManager, Writer errorOutput) {
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		List<String> options = new ArrayList<>();
		options.add("-classpath");
		options.add(System.getProperty("java.class.path"));

		JavaCompiler.CompilationTask task = compiler.getTask(errorOutput, fileManager, diagnostics, options, null, Collections.singletonList(toCompile));

		if (!task.call()) {
			PrintWriter errorWriter = new PrintWriter(errorOutput);
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
				errorWriter.println(diagnostic);
			}
			errorWriter.flush();
			return null;
		}

		Map<String, byte[]> classBytes = fileManager.getClassBytes();
		fileManager.close();

		return classBytes;
	}
}