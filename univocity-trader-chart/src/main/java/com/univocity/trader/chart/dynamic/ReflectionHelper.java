package com.univocity.trader.chart.dynamic;

import org.apache.commons.lang3.*;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class ReflectionHelper {

	public static boolean isFinal(Field field) {
		return (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
	}

	public static Method findSetter(Field f) {
		if (isFinal(f)) {
			throw new IllegalArgumentException(getErrorMessage("Cannot find setter method for final field:", f));
		}

		Method method = null;
		try {
			method = new PropertyDescriptor(f.getName(), f.getDeclaringClass()).getWriteMethod();
		} catch (IntrospectionException e) {
			String setterName = "set" + StringUtils.capitalize(f.getName());

			Class<?>[] types = getPossibleTypes(f.getType());

			for (Class<?> type : types) {
				method = getMethod(setterName, f.getDeclaringClass(), type);
				if (method != null) {
					return method;
				}
			}

			throw new IllegalArgumentException(getErrorMessage("Error finding setter method for:", f), e);
		}

		if (method == null) {
			throw new IllegalArgumentException(getErrorMessage("No setter defined for:", f));
		}

		return method;
	}

	public static Method getMethod(String methodName, Class<?> origin, Class<?>... argTypes) {
		if (origin == Object.class) {
			return null;
		}

		ArrayList<Method> methods = new ArrayList<>();
		Collections.addAll(methods, origin.getMethods());
		Collections.addAll(methods, origin.getDeclaredMethods());
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Class<?>[] paramTypes = method.getParameterTypes();
				if (paramTypes.length != argTypes.length) {
					continue;
				}
				for (int i = 0; i < argTypes.length; i++) {
					if (paramTypes[i] != argTypes[i]) {
						continue;
					}
				}
				return method;
			}
		}

		return getMethod(methodName, origin.getSuperclass(), argTypes);
	}

	private static Class<?>[] getPossibleTypes(Class<?> primitiveOrWrapper) {
		if (isPrimitiveOrWrapper(primitiveOrWrapper)) {
			Class<?>[] types = new Class[2];
			types[0] = primitiveOrWrapper;

			if (primitiveOrWrapper.isPrimitive()) {
				types[1] = getWrapperType(primitiveOrWrapper);
			} else {
				types[1] = getPrimitiveType(primitiveOrWrapper);
			}
			return types;
		}
		return new Class[] { primitiveOrWrapper };
	}

	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() || primitiveWrappers.contains(clazz);
	}

	public static Class<?> getWrapperType(Class<?> clazz) {
		Class<?> out = getPrimitiveOrWrapper(clazz, primitives, wrappers);
		if (out == null) {
			throw new IllegalArgumentException("Not a primitive type: " + clazz);
		}
		return out;
	}

	public static Class<?> getPrimitiveType(Class<?> clazz) {
		Class<?> out = getPrimitiveOrWrapper(clazz, wrappers, primitives);
		if (out == null) {
			throw new IllegalArgumentException("Not a wrapper type: " + clazz);
		}
		return out;
	}

	public static Class<?> getPrimitiveOrWrapper(Class<?> clazz) {
		Class<?> out = getPrimitiveOrWrapper(clazz, wrappers, primitives);
		if (out == null) {
			out = getPrimitiveOrWrapper(clazz, primitives, wrappers);
		}
		if (out == null) {
			throw new IllegalArgumentException("Not a primitive or wrapper type: " + clazz);
		}
		return out;
	}

	private static Class<?> getPrimitiveOrWrapper(Class<?> clazz, Class<?>[] from, Class<?>[] to) {
		for (int i = 0; i < from.length; i++) {
			if (from[i] == clazz) {
				return to[i];
			}
		}
		return null;
	}

	private static final Class<?>[] wrappers = new Class[] { Boolean.class, Byte.class, Character.class, Short.class,
			Integer.class, Long.class, Float.class, Double.class };

	private static final Class<?>[] primitives = new Class[] { boolean.class, byte.class, char.class, short.class,
			int.class, long.class, float.class, double.class };

	private static final Set<?> primitiveWrappers = new HashSet(Arrays.asList(wrappers));

	private static String getErrorMessage(String message, Field f) {
		return message + " " + f.getDeclaringClass().getSimpleName() + "." + f.getName();
	}

	public static Field getField(String fieldName, Class<?> origin) {
		Field[] fields = getFields(fieldName, origin);
		if (fields != null && fields.length > 0) {
			return fields[0];
		}
		return null;
	}

	public static Field[] getFields(String fieldName, Class<?> origin) {
		if (origin == Object.class) {
			return null;
		}

		List<Field> found = new ArrayList<Field>();

		Field[] fields = origin.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				found.add(field);
			}
		}

		Field[] superfields = getFields(fieldName, origin.getSuperclass());
		if (superfields != null) {
			Collections.addAll(found, superfields);
		}

		return found.toArray(new Field[0]);
	}

}
