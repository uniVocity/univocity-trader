package com.univocity.trader.chart.dynamic.code;

import javax.tools.*;
import javax.tools.JavaFileObject.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

final class MemoryJavaFileManager extends ForwardingJavaFileManager {

	private Map<String, byte[]> classBytes;

	public MemoryJavaFileManager(JavaFileManager fileManager) {
		super(fileManager);
		classBytes = new HashMap<>();
	}

	public Map<String, byte[]> getClassBytes() {
		return classBytes;
	}

	public void close() {
		classBytes = null;
	}

	public void flush() {
	}

	private static class SourceCode extends SimpleJavaFileObject {
		final String code;

		SourceCode(String fileName, String code) {
			super(toURI(fileName), Kind.SOURCE);
			this.code = code;
		}

		public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
			return CharBuffer.wrap(code);
		}
	}

	private class CompiledClass extends SimpleJavaFileObject {
		private final String name;

		CompiledClass(String name) {
			super(toURI(name), Kind.CLASS);
			this.name = name;
		}

		public OutputStream openOutputStream() {
			return new FilterOutputStream(new ByteArrayOutputStream()) {
				public void close() throws IOException {
					out.close();
					ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
					classBytes.put(name, bos.toByteArray());
				}
			};
		}
	}

	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (kind == Kind.CLASS) {
			return new CompiledClass(className);
		} else {
			return super.getJavaFileForOutput(location, className, kind, sibling);
		}
	}

	static JavaFileObject makeStringSource(String fileName, String code) {
		return new SourceCode(fileName, code);
	}

	static URI toURI(String name) {
		File file = new File(name);
		if (file.exists()) {
			return file.toURI();
		} else {
			try {
				final StringBuilder newUri = new StringBuilder();
				newUri.append("mfm:///");
				newUri.append(name.replace('.', '/'));
				if (name.endsWith(".java")) {
					newUri.replace(newUri.length() - ".java".length(), newUri.length(), ".java");
				}
				return URI.create(newUri.toString());
			} catch (Exception exp) {
				return URI.create("mfm:///com/sun/script/java/java_source");
			}
		}
	}
}