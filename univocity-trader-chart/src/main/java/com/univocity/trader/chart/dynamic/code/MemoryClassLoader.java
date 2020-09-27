package com.univocity.trader.chart.dynamic.code;

import java.net.*;
import java.util.*;

final class MemoryClassLoader extends URLClassLoader {

	private final Map<String, byte[]> classBytes;

	public MemoryClassLoader(Map<String, byte[]> classBytes) {
		super(new URL[0], ClassLoader.getSystemClassLoader());
		this.classBytes = classBytes;
	}

	protected Class<?> findClass(String className) throws ClassNotFoundException {
		if(classBytes == null){
			return super.findClass(className);
		}
		byte[] bytes = classBytes.get(className);
		if (bytes != null) {
			classBytes.put(className, null);
			return defineClass(className, bytes, 0, bytes.length);
		} else {
			return super.findClass(className);
		}
	}
}