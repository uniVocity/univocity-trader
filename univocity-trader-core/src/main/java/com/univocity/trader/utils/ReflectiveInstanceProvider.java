package com.univocity.trader.utils;

import com.univocity.trader.simulation.*;
import org.slf4j.*;

import java.lang.reflect.*;

/**
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ReflectiveInstanceProvider<T> implements InstanceProvider<T> {

	private static final Logger log = LoggerFactory.getLogger(ReflectiveInstanceProvider.class);

	private final Class<T> type;

	private Constructor noArgs;
	private Constructor params;
	private Constructor symbol;
	private Constructor symbolAndParams;
	private Constructor paramsAndSymbol;

	public ReflectiveInstanceProvider(Class<T> type) {
		this.type = type;

		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			if ((!Modifier.isPublic(constructor.getModifiers())
					|| !Modifier.isPublic(constructor.getDeclaringClass().getModifiers()))
					&& !constructor.isAccessible()) {
				continue;
			}
			Class<?>[] args = constructor.getParameterTypes();
			if (args.length == 0) {
				noArgs = constructor;
			} else if (args.length == 1) {
				if (args[0] == String.class) {
					symbol = constructor;
				} else if (Parameters.class.isAssignableFrom(args[0])) {
					params = constructor;
				}
			} else if (args.length == 2) {
				if (args[0] == String.class && Parameters.class.isAssignableFrom(args[1])) {
					symbolAndParams = constructor;
				} else if (Parameters.class.isAssignableFrom(args[1]) && args[0] == String.class) {
					paramsAndSymbol = constructor;
				}
			}
		}
	}

	@Override
	public T create(String symbol, Parameters params) {
		T out = tryNewInstance(symbolAndParams, symbol, params);
		if (out == null) {
			out = tryNewInstance(paramsAndSymbol, params, symbol);
		}
		if (out == null) {
			out = tryNewInstance(this.params, params);
		}
		if (out == null) {
			out = tryNewInstance(this.symbol, symbol);
		}
		if (out == null) {
			out = tryNewInstance(this.noArgs);
		}
		if (out == null) {
			throw new IllegalStateException("Unable to create new instance of " + type.getName());
		}
		return out;
	}

	private T tryNewInstance(Constructor constructor, Object... params) {
		if (constructor == null) {
			return null;
		}
		try {
			return (T) constructor.newInstance(params);
		} catch (Exception e) {
			log.error("Unable to invoke constructor '" + constructor.toString() + "' of class " + type.getName(), e);
		}
		return null;
	}
}
