/*******************************************************************************
 * Copyright 2014 Univocity Software Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.univocity.trader.config;

import io.github.classgraph.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.reflect.Array.*;
import static java.sql.Connection.*;

/**
 * An utility class for validating inputs.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 */
public class Utils {

	public static final String systemLineSeparatorString;
	private static final char[] systemLineSeparator;

	static {
		String lineSeparator = System.getProperty("line.separator");
		if (lineSeparator == null) {
			systemLineSeparatorString = "\n";
		} else {
			systemLineSeparatorString = lineSeparator;
		}
		systemLineSeparator = systemLineSeparatorString.toCharArray();
	}


	/**
	 * An empty String array.
	 */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/**
	 * Throws an IllegalArgumentException if the given array is null or empty.
	 *
	 * @param argDescription the description of the elements
	 * @param args           the elements to be validated.
	 * @param <T>            Type of arguments to be validated
	 */
	public static <T> void notEmpty(String argDescription, T... args) {
		if (args == null) {
			throw new IllegalArgumentException(argDescription + " must not be null");
		}
		if (args.length == 0) {
			throw new IllegalArgumentException(argDescription + " must not be empty");
		}
	}

	/**
	 * Throws an IllegalArgumentException if the given array is null,empty, or
	 * contains null values
	 *
	 * @param argDescription the description of the elements
	 * @param args           the elements to be validated.
	 * @param <T>            Type of arguments to be validated
	 */
	public static <T> void noNulls(String argDescription, T... args) {
		notEmpty(argDescription, args);
		for (T arg : args) {
			if (arg == null) {
				if (args.length > 0) {
					throw new IllegalArgumentException(argDescription + " must not contain nulls");
				} else {
					throw new IllegalArgumentException(argDescription + " must not be null");
				}
			}
		}
	}

	/**
	 * Returns the indexes of an element in a given array.
	 *
	 * @param array   the element array
	 * @param element the element to be looked for in the array.
	 *
	 * @return the indexes of the given element in the array, or an empty array
	 * if no element could be found
	 */
	public static int[] indexesOf(Object[] array, Object element) {
		int[] tmp = new int[0];

		int i = 0;
		int o = 0;
		while (i < array.length) {
			i = indexOf(array, element, i);
			if (i == -1) {
				break;
			}

			tmp = Arrays.copyOf(tmp, tmp.length + 1);
			tmp[o++] = i;
			i++;
		}

		return tmp;
	}

	/**
	 * Returns the index of an element in a given array.
	 *
	 * @param array   the element array
	 * @param element the element to be looked for in the array.
	 *
	 * @return the index of the given element in the array, or -1 if the element
	 * could not be found.
	 */
	public static int indexOf(Object[] array, Object element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Returns the index of an element in a given array.
	 *
	 * @param array   the element array
	 * @param element the element to be looked for in the array.
	 * @param from    the starting position of the array from where to start the
	 *                search
	 *
	 * @return the index of the given element in the array, or -1 if the element
	 * could not be found.
	 */
	private static int indexOf(Object[] array, Object element, int from) {
		if (array == null) {
			throw new NullPointerException("Null array");
		}
		if (element == null) {
			for (int i = from; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			if (element.getClass() != array.getClass().getComponentType()) {
				throw new IllegalStateException("a");
			}
			if (element instanceof String && array instanceof String[]) {
				for (int i = from; i < array.length; i++) {
					String e = String.valueOf(array[i]);
					if (element.toString().equalsIgnoreCase(e)) {
						return i;
					}
				}
			} else {
				for (int i = from; i < array.length; i++) {
					if (element.equals(array[i])) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Searches for elements in a given array and returns the elements not
	 * found.
	 *
	 * @param array    An array with elements
	 * @param elements the elements to be found
	 *
	 * @return the elements not found in the array.
	 */
	public static Object[] findMissingElements(Object[] array, Collection<?> elements) {
		return findMissingElements(array, elements.toArray());
	}

	/**
	 * Searches for elements in a given array and returns the elements not
	 * found.
	 *
	 * @param array    An array with elements
	 * @param elements the elements to be found
	 *
	 * @return the elements not found in the array.
	 */
	public static Object[] findMissingElements(Object[] array, Object[] elements) {
		List<Object> out = new ArrayList<Object>();

		for (Object element : elements) {
			if (indexOf(array, element) == -1) {
				out.add(element);
			}
		}

		return out.toArray();
	}

	/**
	 * Creates a {@link java.io.Writer} from an output stream
	 *
	 * @param output the output stream
	 *
	 * @return {@link java.io.Writer} wrapping the given output stream
	 */
	public static Writer newWriter(OutputStream output) {
		return newWriter(output, (Charset) null);
	}

	/**
	 * Creates a {@link java.io.Writer} from an output stream
	 *
	 * @param output   the output stream
	 * @param encoding the encoding to use when writing to the output stream
	 *
	 * @return {@link java.io.Writer} wrapping the given output stream
	 */
	public static Writer newWriter(OutputStream output, String encoding) {
		return newWriter(output, Charset.forName(encoding));
	}

	/**
	 * Creates a {@link java.io.Writer} from an output stream
	 *
	 * @param output   the output stream
	 * @param encoding the encoding to use when writing to the output stream
	 *
	 * @return {@link java.io.Writer} wrapping the given output stream
	 */
	public static Writer newWriter(OutputStream output, Charset encoding) {
		if (encoding != null) {
			return new OutputStreamWriter(output, encoding);
		} else {
			return new OutputStreamWriter(output);
		}
	}

	/**
	 * Creates a {@link java.io.Writer} from a file
	 *
	 * @param file the file to be written
	 *
	 * @return {@link java.io.Writer} for the given file
	 */
	public static Writer newWriter(File file) {
		return newWriter(file, (Charset) null);
	}

	/**
	 * Creates a {@link java.io.Writer} from a file
	 *
	 * @param file     the file to be written
	 * @param encoding the encoding to use when writing to the file
	 *
	 * @return {@link java.io.Writer} for the given file
	 */
	public static Writer newWriter(File file, String encoding) {
		return newWriter(file, Charset.forName(encoding));
	}

	/**
	 * Creates a {@link java.io.Writer} from a file
	 *
	 * @param file     the file to be written
	 * @param encoding the encoding to use when writing to the file
	 *
	 * @return {@link java.io.Writer} for the given file
	 */
	public static Writer newWriter(File file, Charset encoding) {
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to create file '" + file.getAbsolutePath() + "', please ensure your application has permission to create files in that path", e);
			}
		}

		FileOutputStream os;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}

		return newWriter(os, encoding);
	}


	/**
	 * Converts a list of enumerations to an array of their {@link
	 * Enum#toString()} representation
	 *
	 * @param enums a list of enumerations to convert
	 *
	 * @return an array of {@code String} with the values produced by each
	 * element's {@link Enum#toString()} method.
	 */

	public static String[] toArray(List<Enum> enums) {
		String[] out = new String[enums.size()];

		for (int i = 0; i < out.length; i++) {
			out[i] = enums.get(i).toString();
		}

		return out;

	}

	/**
	 * Converts any collection of {@code Integer} into an {@code int} array.
	 *
	 * @param ints a collection of (boxed) integers.
	 *
	 * @return a primitive {@code int} array with the unboxed integer values.
	 */
	public static int[] toIntArray(Collection<Integer> ints) {
		int[] out = new int[ints.size()];

		int i = 0;
		for (Integer boxed : ints) {
			out[i++] = boxed.intValue();
		}

		return out;

	}

	/**
	 * Converts any collection of {@code Character} into a char array.
	 *
	 * @param characters a collection of (boxed) characters.
	 *
	 * @return a primitive {@code char} array with the unboxed character values.
	 */
	public static char[] toCharArray(Collection<Character> characters) {
		char[] out = new char[characters.size()];

		int i = 0;
		for (Character boxed : characters) {
			out[i++] = boxed.charValue();
		}

		return out;
	}

	/**
	 * Restricts the length of a given content.
	 *
	 * @param length  the maximum length to be displayed. If {@code 0}, the
	 *                {@code "<omitted>"} string will be returned.
	 * @param content the content whose length should be restricted.
	 *
	 * @return the restricted content.
	 */
	public static String restrictContent(int length, CharSequence content) {
		if (content == null) {
			return null;
		}
		if (length == 0) {
			return "<omitted>";
		}
		if (length == -1) {
			return content.toString();
		}

		int errorMessageStart = content.length() - length;
		if (length > 0 && errorMessageStart > 0) {
			return "..." + content.subSequence(errorMessageStart, content.length()).toString();
		}
		return content.toString();
	}

	/**
	 * Restricts the length of a given content.
	 *
	 * @param length  the maximum length to be displayed. If {@code 0}, the
	 *                {@code "<omitted>"} string will be returned.
	 * @param content the content whose length should be restricted.
	 *
	 * @return the restricted content.
	 */
	public static String restrictContent(int length, Object content) {
		if (content == null) {
			return null;
		}
		if (content instanceof Object[]) {
			return restrictContent(length, Arrays.toString((Object[])content));
		}
		return restrictContent(length, String.valueOf(content));
	}

	/**
	 * Allows rethrowing a checked exception instead of wrapping it into a
	 * runtime exception. For internal use only
	 * as this generally causes more trouble than it solves (your
	 * exception-specific catch statement may not catch this
	 * error - make sure you are catching a Throwable)
	 *
	 * @param error the (potentially checked) exception to the thrown.
	 */
	public static void throwUnchecked(Throwable error) {
		Utils.<RuntimeException>throwsUnchecked(error);
	}

	private static <T extends Exception> void throwsUnchecked(Throwable toThrow) throws T {
		throw (T) toThrow;
	}

	/**
	 * Converts a sequence of int numbers into a byte array.
	 *
	 * @param ints the integers to be cast to by
	 *
	 * @return the resulting byte array.
	 */
	public static byte[] toByteArray(int... ints) {
		byte[] out = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			out[i] = (byte) ints[i];
		}
		return out;
	}

	/**
	 * Identifies duplicate values in a given array and returns them
	 *
	 * @param array the search array
	 * @param <T>   the type of elements held in the given array.
	 *
	 * @return all duplicate values found in the given array, or empty array if
	 * no duplicates, or {@code null} if the input is {@code null}.
	 */
	public static <T> T[] findDuplicates(T[] array) {
		if (array == null || array.length == 0) {
			return array;
		}

		Set<T> elements = new HashSet<T>(array.length);
		ArrayList<T> duplicates = new ArrayList<T>(1);

		for (T element : array) {
			if (!elements.contains(element)) {
				elements.add(element);
			} else {
				duplicates.add(element);
			}
		}

		return duplicates.toArray((T[]) newInstance(array.getClass().getComponentType(), duplicates.size()));
	}

	/**
	 * Removes surrounding spaces from a given {@code String}, from its right or
	 * left side, or both.
	 *
	 * @param input the content to trim
	 * @param left  flag to indicate whether spaces on the left side of the
	 *              string should be removed.
	 * @param right flag to indicate whether spaces on the right side of the
	 *              string should be removed.
	 *
	 * @return the trimmed string.
	 */
	public static String trim(String input, boolean left, boolean right) {
		if (input.length() == 0 || !left && !right) {
			return input;
		}
		int begin = 0;
		while (left && begin < input.length() && input.charAt(begin) <= ' ') {
			begin++;
		}
		if (begin == input.length()) {
			return "";
		}

		int end = begin + input.length() - 1;
		if (end >= input.length()) {
			end = input.length() - 1;
		}

		while (right && input.charAt(end) <= ' ') {
			end--;
		}

		if (begin == end) {
			return "";
		}

		if (begin == 0 && end == input.length() - 1) {
			return input;
		}

		return input.substring(begin, end + 1);
	}

	/**
	 * Returns the system's line separator sequence, which can contain 1 to 2
	 * characters.
	 *
	 * @return a sequence of 1 to 2 characters used as the system's line ending.
	 */
	public static char[] systemLineSeparator() {
		return systemLineSeparator.clone();
	}

	/**
	 * Joins the {@code String} representation of all non-null values in a given
	 * collection into a {@code String}, with a given separator between each value.
	 *
	 * @param values    the values to be joined. Nulls are skipped.
	 * @param separator the separator to use between each value
	 *
	 * @return a String with all non-null values in the given collection.
	 */
	public static final String join(Iterable<?> values, String separator) {
		if (values == null) {
			return "";
		}

		StringBuilder out = new StringBuilder(64);
		for (Object value : values) {
			if (value != null) {
				if (out.length() != 0) {
					out.append(separator);
				}
				out.append(value);
			}
		}

		return out.toString();
	}

	/**
	 * Joins each collection of values in a given {@code Map} into their {@code String}
	 * representation, with a given separator between each value.
	 *
	 * @param map       a map containing collections as its values
	 * @param separator the separator to be used between each value
	 * @param <K>       the type of the key used in the given map
	 * @param <V>       the type of the collection of values associated with each key of the map
	 *
	 * @return the resulting map where each key of the given input map is associated
	 * with the String representation of all non-null values in the collection
	 * associated with the key.
	 */
	public static final <K, V extends Iterable> Map<K, String> joinValues(Map<K, V> map, String separator) {
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		LinkedHashMap<K, String> out = new LinkedHashMap<K, String>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			out.put(e.getKey(), join(e.getValue(), separator));
		}
		return out;
	}

	/**
	 * Returns the {@code Map.Entry} stored in a map by searching for a given {@code String}
	 * key case-insensitively.
	 *
	 * @param map the map to search
	 * @param key the key to look for
	 * @param <V> the type of values stored in the map
	 *
	 * @return the {@code Map.Entry} associated with the given key, or {@code null} if not found.
	 */
	public static final <V> Map.Entry<String, V> getEntryCaseInsensitive(Map<String, V> map, String key) {
		if (key != null) {
			key = key.toLowerCase(Locale.ENGLISH);
		}

		for (Map.Entry<String, V> entry : map.entrySet()) {
			String k = entry.getKey();
			if (key == null) {
				if (k == null) {
					return entry;
				}
			} else if (k != null && key.equals(k.toLowerCase(Locale.ENGLISH))) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Returns the value stored in a map by searching for a given {@code String}
	 * key case-insensitively.
	 *
	 * @param map the map to search
	 * @param key the key to look for
	 * @param <V> the type of values stored in the map
	 *
	 * @return the value associated with the given key, or {@code null} if not found.
	 */
	public static final <V> V getValueCaseInsensitive(Map<String, V> map, String key) {
		Map.Entry<String, V> e = getEntryCaseInsensitive(map, key);
		if (e == null) {
			return null;
		}
		return e.getValue();
	}

	/**
	 * Adds a given value into a list of values of a map, where the key should be handled case-insensitively.
	 *
	 * @param map   the map into which the value will be added
	 * @param key   the key to look for
	 * @param value the value to add
	 * @param add   a flag indicating whether the value should be added to the list of existing values, or if
	 *              the list should be replaced by a new one containing only the given value.
	 * @param <V>   the type of the values stored in the map.
	 */
	public static final <V> void putValueCaseInsensitive(Map<String, List<V>> map, String key, V value, boolean add) {
		List<V> values = Utils.getValueCaseInsensitive(map, key);
		if (values == null) {
			values = new ArrayList<V>();
			map.put(key, values);
		} else if (!add) {
			values.clear();
		}
		values.add(value);
	}

	/**
	 * Ensures a given argument is not null.
	 *
	 * @param o         the object to validate
	 * @param fieldName the description of the field
	 */
	public static final void notNull(Object o, String fieldName) {
		if (o == null) {
			throw new IllegalArgumentException(fieldName + " cannot be null");
		}
	}

	/**
	 * Ensures a given number is positive (and greater than zero).
	 *
	 * @param o         the number to validate
	 * @param fieldName the description of the field
	 */
	public static final void positive(Number o, String fieldName) {
		notNull(o, fieldName);
		if (((Integer) o.intValue()).compareTo(0) <= 0) {
			throw new IllegalArgumentException(fieldName + " must be positive. Got " + o);
		}
	}

	/**
	 * Ensures a given number is positive or equal to zero.
	 *
	 * @param o         the number to validate
	 * @param fieldName the description of the field
	 */
	public static final void positiveOrZero(Number o, String fieldName) {
		notNull(o, fieldName);
		if (((Double) o.doubleValue()).compareTo(0.0) < 0) {
			throw new IllegalArgumentException(fieldName + " must be a positive number or zero. Got " + o);
		}
	}

	/**
	 * Ensures a given array argument is not null/empty and no elements are null/empty
	 *
	 * @param sequence  the array of objects
	 * @param fieldName the description of the field
	 * @param <T>       the type of elements in the array
	 */
	public static <T> void notEmpty(T[] sequence, String fieldName) {
		notNull(sequence, fieldName);
		if (sequence.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
		for (T element : sequence) {
			if (element == null) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Null elements are not allowed. Got " + Arrays.toString(sequence));
			} else if (element instanceof String && element.toString().trim().isEmpty()) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Blank elements are not allowed. Got " + Arrays.toString(sequence));
			}
		}
	}

	/**
	 * Ensures a given collection is not null/empty
	 *
	 * @param field     the collection of objects
	 * @param fieldName the description of the field
	 */
	public static void notEmpty(Collection<?> field, String fieldName) {
		notNull(field, fieldName);
		if (field.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given collection is not null/empty and doesn't contain null/empty objects or blank Strings
	 *
	 * @param elements  the collection of objects
	 * @param fieldName the description of the field
	 * @param <T>       the type of elements in the collection
	 */
	public static <T> void noneEmpty(Collection<T> elements, String fieldName) {
		notNull(elements, fieldName);
		if (elements.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
		for (T element : elements) {
			if (element == null) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Null elements are not allowed. Got " + elements);
			} else if (element instanceof String && element.toString().trim().isEmpty()) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Blank elements are not allowed. Got " + elements);
			}
		}
	}

	/**
	 * Ensures a given int[] array argument is not null/empty
	 *
	 * @param field     the array of objects
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(int[] field, String fieldName) {
		notNull(field, fieldName);
		if (field.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given {@code char} array argument is not null/empty
	 *
	 * @param field     the array of objects
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(char[] field, String fieldName) {
		notNull(field, fieldName);
		if (field.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}


	/**
	 * Ensures a given {@link CharSequence} argument is not null/empty
	 *
	 * @param o         a character sequence
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(CharSequence o, String fieldName) {
		notNull(o, fieldName);
		if (o.length() == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given {@link CharSequence} argument is not null/empty/blank
	 *
	 * @param o         a character sequence
	 * @param fieldName the description of the field
	 */
	public static final void notBlank(CharSequence o, String fieldName) {
		notNull(o, fieldName);
		if (o.toString().trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
	}

	/**
	 * Ensures the elements in a given array are not null/empty/blank. The array itself can be empty but not null.
	 *
	 * @param o         the array of elements to be validated.
	 * @param fieldName description of the array.
	 */
	public static final void noBlanks(Object[] o, String fieldName) {
		notNull(o, fieldName);
		for (Object e : o) {
			if (e == null) {
				throw new IllegalArgumentException("Null value in " + fieldName + ": " + Arrays.toString(o));
			}
			if (e instanceof CharSequence) {
				if (isBlank(e.toString())) {
					throw new IllegalArgumentException("Blank value in " + fieldName + ": " + Arrays.toString(o));
				}
			}
		}
	}

	/**
	 * Ensures a given {@link File} argument is not null, exists and does not point to a directory
	 *
	 * @param file      a file
	 * @param fieldName the description of the field
	 */
	public static final void validFile(File file, String fieldName) {
		notNull(file, fieldName);
		if (!file.exists()) {
			throw new IllegalArgumentException("Illegal " + fieldName + ": '" + file.getAbsolutePath() + "' it does not exist.");
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException("Illegal " + fieldName + ": '" + file.getAbsolutePath() + "' it cannot be a directory.");
		}
	}

	/**
	 * Attempts to discover the plain name of a given file, without directories or its extension
	 *
	 * @param name      a name if known. Will be returned if provided.
	 * @param file      a file whose path will be analyzed in order to extract a name from
	 * @param fieldName name of field associated with the file name. Used for validation messages only
	 *
	 * @return the given name or the file name if possible. If neither can be provided,
	 * an {@code IllegalArgumentException} will be thrown
	 */
	public static final String guessAndValidateName(String name, File file, String fieldName) {
		if (name != null) {
			notBlank(name, fieldName);
			return name;
		}
		validFile(file, fieldName);

		name = file.getName();
		if (name.lastIndexOf('.') != -1) {
			name = name.substring(0, name.lastIndexOf('.'));
		}

		if (name.trim().isEmpty()) {
			throw new IllegalArgumentException("Cannot derive " + fieldName + " from file " + file.getAbsolutePath());
		}

		return name;
	}


	/**
	 * Tests if a given {@code String} is null/empty/blank/
	 *
	 * @param s the string
	 *
	 * @return {@code true} if the given {@code String} is null, empty or blank, otherwise returns {@code false}
	 */
	public static final boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	/**
	 * Tests if a given {@code String} is not null/empty/blank/
	 *
	 * @param s the string
	 *
	 * @return {@code true} if the given {@code String} is not null, empty or blank, otherwise returns {@code false}
	 */
	public static final boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	/**
	 * Replaces system properties between { and } in a given {@code String} with the property values, and returns the result.
	 * Unknown properties won't be replaced.
	 *
	 * @param string the {@code String} with potential system properties.
	 *
	 * @return the resulting {@code String} with all known system properties replaced.
	 */
	public static final String replaceSystemProperties(String string) {
		int offset = 0;
		while (true) {
			int braceOpen = string.indexOf('{', offset);
			if (braceOpen >= 0) {
				offset = braceOpen;
				int braceClose = string.indexOf('}');
				if (braceClose > braceOpen) {
					offset = braceClose;
					String property = string.substring(braceOpen + 1, braceClose);
					String value = System.getProperty(property);
					if (value != null) {
						String beforeProperty = string.substring(0, braceOpen);
						String afterProperty = "";
						if (braceClose < string.length()) {
							afterProperty = string.substring(braceClose + 1);
						}
						string = beforeProperty + value + afterProperty;
					}
				}
			} else {
				return string;
			}
		}
	}

	/**
	 * Decodes a URL encoded value using UTF-8.
	 *
	 * @param value the value to be decoded.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(Object value) {
		return decode(null, value, null);
	}

	/**
	 * Decodes a URL encoded value.
	 *
	 * @param value       the value to be decoded.
	 * @param charsetName the charset to use for decoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(Object value, String charsetName) {
		return decode(null, value, charsetName);
	}

	/**
	 * Decodes a URL encoded value.
	 *
	 * @param parameterName name of the parameter associated with the value
	 * @param value         the value to be decoded.
	 * @param charsetName   the charset to use for decoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(String parameterName, Object value, String charsetName) {
		if (value == null) {
			return null;
		}
		if (charsetName == null) {
			charsetName = "UTF-8";
		}
		String stringVal = String.valueOf(value);
		try {
			stringVal = URLDecoder.decode(stringVal, charsetName);
		} catch (Exception ex) {
			if (parameterName == null) {
				throw new IllegalStateException("Error decoding value: " + value, ex);
			} else {
				throw new IllegalStateException("Error decoding value of parameter '" + parameterName + "'. Value: " + value, ex);
			}
		}

		return stringVal;
	}

	/**
	 * Encodes a value using UTF-8 so it can be used as part of a URL.
	 *
	 * @param parameterValue the value to be encoded.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(Object parameterValue) {
		return encode(null, parameterValue, null);
	}

	/**
	 * Encodes a value so it can be used as part of a URL.
	 *
	 * @param parameterValue the value to be encoded.
	 * @param charsetName    charset to use for encoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(Object parameterValue, String charsetName) {
		return encode(null, parameterValue, charsetName);
	}

	/**
	 * Encodes a value so it can be used as part of a URL.
	 *
	 * @param parameterName  name of the parameter associated with the value
	 * @param parameterValue the value to be encoded.
	 * @param charsetName    charset to use for encoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(String parameterName, Object parameterValue, String charsetName) {
		if (parameterValue == null) {
			return null;
		}
		if (charsetName == null) {
			charsetName = "UTF-8";
		}
		String original = String.valueOf(parameterValue);

		try {
			return URLEncoder.encode(original, charsetName);
		} catch (Exception ex) {
			if (parameterName == null) {
				throw new IllegalStateException("Error encoding value: " + parameterValue, ex);
			} else {
				throw new IllegalStateException("Error encoding value of parameter '" + parameterName + "'. Value: " + parameterValue, ex);
			}
		}
	}

	/**
	 * Converts a yyyy-MM-dd formatted string to a Calendar instance.
	 *
	 * @param s the yyyy-MM-dd formatted string
	 *
	 * @return the corresponding {@code Calendar} instance
	 */
	public static final Calendar isoDateStringToCalendar(String s) {
		if (isBlank(s)) {
			return null;
		}
		Calendar out = null;
		try {
			int firstDash = s.indexOf('-');
			int secondDash = s.indexOf('-', firstDash + 1);

			String yyyy = s.substring(0, firstDash);
			String mm = s.substring(firstDash + 1, secondDash);
			String dd = s.substring(secondDash + 1);

			if (yyyy.length() == 4 && mm.length() == 2 && dd.length() == 2) {
				int year = Integer.parseInt(yyyy);
				int month = Integer.parseInt(mm) - 1;
				int day = Integer.parseInt(dd);

				out = new GregorianCalendar(year, month, day);

				if (out.get(Calendar.YEAR) != year || out.get(Calendar.MONTH) != month || out.get(Calendar.DAY_OF_MONTH) != day) {
					out = null;
				}
			}
		} catch (Exception e) {
			//Not formatted correctly ignore any errors here;
		}

		if (out == null) {
			throw new IllegalArgumentException("Date '" + s + "' must be formatted as yyyy-MM-dd");
		}

		return out;
	}

	/**
	 * Determines whether two collections of Object[] contain the same values.
	 *
	 * @param c1 the first collection
	 * @param c2 the second collection.
	 *
	 * @return {@code true} if both collections contain the same values, {@code false} otherwise.
	 */
	public static boolean equals(Collection<Object[]> c1, Collection<Object[]> c2) {
		if (c1 == c2) {
			return true;
		}
		if (c1 != null) {
			if (c2 != null) {
				if (c1.size() == c2.size()) {
					Iterator<Object[]> i1 = c1.iterator();
					Iterator<Object[]> i2 = c2.iterator();

					while (i1.hasNext()) {
						if (!Arrays.equals(i1.next(), i2.next())) {
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return c2 == null;
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalStateException}
	 * if the expression evaluates to {@code false}.
	 * <pre class="code">assertState(id == null, "The id property must not already be initialized");</pre>
	 *
	 * @param expression a boolean expression
	 * @param message    the exception message to use if the assertion fails
	 *
	 * @throws IllegalStateException if {@code expression} is {@code false}
	 */
	public static void assertState(boolean expression, String message) {
		if (!expression) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Ensures a given SQL isolation level is a valid and known JDBC value that exists int {@link java.sql.Connection}
	 *
	 * @param transactionIsolationLevel code of the transaction isolation level
	 */
	public static void validTransactionIsolationLevel(int transactionIsolationLevel) {
		List<Integer> levels = Arrays.asList(TRANSACTION_NONE, TRANSACTION_READ_COMMITTED, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE);
		if (!levels.contains(transactionIsolationLevel)) {
			throw new IllegalArgumentException("Illegal transaction isolation level: " + transactionIsolationLevel + ". Accepted isolation levels are: " + levels + " (from java.sql.Connection)");
		}
	}

	public static String toString(Object o) {
		return o == null ? null : o.toString();
	}

	/**
	 * Displays line separators in a string by replacing all instances
	 * of `\r` and `\n` with `[cr]` and `[lf]`.
	 * If `\r` is followed by `\n` or vice versa, then `[crlf]` or `[lfcr]` will be printed.
	 *
	 * @param str        the string to have its line separators displayed
	 * @param addNewLine flag indicating whether the original `\r` or `\n` characters should be kept in the string.
	 *                   if {@code true}, `\r` will be replaced by `[cr]\r` for example.
	 *
	 * @return the updated string with any line separators replaced by visible character sequences.
	 */
	public static String displayLineSeparators(String str, boolean addNewLine) {

		StringBuilder out = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '\r' || ch == '\n') {
				out.append('[');

				out.append(ch == '\r' ? "cr" : "lf");

				char next = '\0';
				if (i + 1 < str.length()) {
					next = str.charAt(i + 1);
					if (next != ch && (next == '\r' || next == '\n')) {
						out.append(next == '\r' ? "cr" : "lf");
						i++;
					} else {
						next = '\0';
					}
				}

				out.append(']');

				if (addNewLine) {
					out.append(ch);
					if (next != '\0') {
						out.append(next);
					}
				}
			} else {
				out.append(ch);
			}
		}

		return out.toString();
	}

	/**
	 * Removes all instances of a given element from an int array.
	 *
	 * @param array the array to be checked
	 * @param e     the element to be removed
	 *
	 * @return an updated array that does not contain the given element anywhere,
	 * or the original array if the element has not been found.
	 */
	public static int[] removeAll(int[] array, int e) {
		if (array == null || array.length == 0) {
			return array;
		}

		int removeCount = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == e) {
				removeCount++;
			}
		}

		if (removeCount == 0) {
			return array;
		}

		int[] tmp = new int[array.length - removeCount];
		for (int i = 0, j = 0; i < array.length; i++) {
			if (array[i] != e) {
				tmp[j++] = array[i];
			}
		}
		return tmp;
	}

	public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
		Constructor<T> c = null;
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (constructor.getParameterTypes().length == 0) {
				c = (Constructor<T>) constructor;
				break;
			}
		}

		if (c != null) {
			try {
				c.setAccessible(true);
			} catch (Exception e) {
				//ignore if unable.
			}
		}
		return c;
	}

	/**
	 * Finds all classes that extend from a given class or implement a given interface.
	 *
	 * @param classesToSearch a map where the key is the parent class/interface and the set of values are the names
	 *                        of the classes to find which extend from the given class or implement the given interface.
	 *                        The names don't have to contain the full package and in this case the classpath will be
	 *                        traversed to find a matching name.
	 *
	 * @return the search results in a map where the key is each parent class/interface given in the {@code classesToSearch} map
	 * and the values are the a set the classes found, where each class has the name given in the original {@code classesToSearch} map.
	 */
	public static Map<Class<?>, LinkedHashSet<Class<?>>> findClasses(Map<Class<?>, LinkedHashSet<String>> classesToSearch) {
		Map<Class<?>, LinkedHashSet<Class<?>>> out = new HashMap<>();

		classesToSearch.forEach((k, v) -> findClasses(scanClasses(), k, v, out));
		return out;
	}

	/**
	 * Finds a class that extends from a given class or implement a given interface.
	 *
	 * @param parent      the parent class/interface whose subclasses/implementations will be searched
	 * @param classToFind Name of the class to find which extends from the given class or implements the given interface.
	 *                    The name doesn't have to contain the full package and in this case the classpath will be
	 *                    searched to fina a matching name.
	 *
	 * @return the class found
	 *
	 * @throws IllegalStateException    if multiple classes are found.
	 * @throws IllegalArgumentException if no class is found.
	 */
	public static <T> Class<? extends T> findClass(Class<T> parent, String classToFind) {
		return (Class<? extends T>) search(scanClasses(), parent, Collections.singleton(classToFind)).iterator().next();
	}

	private static WeakReference<ScanResult> classesScanned;

	private static synchronized ScanResult scanClasses() {
		ScanResult out;
		if (classesScanned == null || (out = classesScanned.get()) == null) {
			classesScanned = new WeakReference<>(out = runClassScan()) {
				@Override
				public void clear() {
					super.clear();
					try {
						ScanResult.closeAll();
					} catch (Throwable t) {
						//ignore.
					}
				}
			};
		}
		return out;
	}

	private static ScanResult runClassScan() {
		return new ClassGraph().enableClassInfo().ignoreClassVisibility().blacklistPackages("java.*").scan();
	}

	/**
	 * Finds a class that extends from a given class or implement a given interface, and returns a new instance
	 * of that class via its (expected default no-arg constructor
	 *
	 * @param parent      the parent class/interface whose subclasses/implementations will be searched
	 * @param classToFind Name of the class to find which extends from the given class or implements the given interface.
	 *                    The name doesn't have to contain the full package and in this case the classpath will be
	 *                    searched to fina a matching name.
	 *
	 * @return an instance of the class found
	 *
	 * @throws IllegalStateException if none or multiple classes are found, or if it's not possible to create a new instance
	 */
	public static <T> T findClassAndInstantiate(Class<T> parent, String classToFind) {
		try {
			Class<? extends T> clazz = (Class<? extends T>) Utils.findClass(parent, classToFind);
			return Utils.getDefaultConstructor(clazz).newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to load class '" + classToFind + "'", ex);
		}
	}

	private static void doSearch(boolean caseInsensitive, List<Class<?>> loadedClasses, Set<String> classNames, LinkedHashSet<Class<?>> sorted) {
		Iterator<String> names = classNames.iterator();
		while (names.hasNext()) {
			String name = names.next();
			boolean found = false;
			Iterator<Class<?>> it = loadedClasses.iterator();
			while (it.hasNext()) {
				Class<?> c = it.next();
				if ((caseInsensitive && c.getName().toLowerCase().endsWith(name.toLowerCase())) || c.getName().endsWith(name)) {
					it.remove();
					names.remove();
					found = true;
					sorted.add(c);
					break;
				}
			}
			if (caseInsensitive && !found) {
				throw new IllegalArgumentException("Could not find class '" + name + "'");
			}
		}
	}

	private static LinkedHashSet<Class<?>> search(ScanResult scanResult, Class<?> parentType, Set<String> classNames) {
		ClassInfoList subclasses = parentType.isInterface() ? scanResult.getClassesImplementing(parentType.getName()) : scanResult.getSubclasses(parentType.getName());

		String[] lowerCasedNames = classNames.toArray(new String[0]);
		for (int i = 0; i < lowerCasedNames.length; i++) {
			lowerCasedNames[i] = lowerCasedNames[i].toLowerCase();
		}

		List<Class<?>> loadedClasses = subclasses.stream()
				.filter(subClass -> subClass.isStandardClass() && !subClass.isAbstract() && !subClass.isAnonymousInnerClass())
				.filter(subClass -> StringUtils.endsWithAny(subClass.getName().toLowerCase(), lowerCasedNames))
				.map(ClassInfo::loadClass)
				.collect(Collectors.toList());
		;

		LinkedHashSet<Class<?>> sorted = new LinkedHashSet<>();
		classNames = new LinkedHashSet<>(classNames);
		doSearch(false, loadedClasses, classNames, sorted);
		doSearch(true, loadedClasses, classNames, sorted);

		if (!loadedClasses.isEmpty()) {
			StringBuilder simpleNames = new StringBuilder();
			for (Class c : loadedClasses) {
				if (simpleNames.length() > 0) {
					simpleNames.append(", ");
				}
				simpleNames.append(c.getSimpleName());
			}
			throw new IllegalStateException("Loaded multiple classes with the same name: " + simpleNames + ". Please provide the class package to disambiguate");
		}

		return sorted;
	}

	private static void findClasses(ScanResult scanResult, Class<?> parentType, LinkedHashSet<String> classNames, Map<Class<?>, LinkedHashSet<Class<?>>> out) {
		out.put(parentType, search(scanResult, parentType, classNames));
	}

	static <T> void parseGroupSetting(PropertyBasedConfiguration properties, String propertyName, Function<String, T> valueTransform, BiConsumer<T, String[]> consumer) {
		//e.g. trade.minimum.amount=[ADA;XRP]50.5, [BTC]100, 30
		List<String> settingsPerGroup = properties.getOptionalList(propertyName);
		for (String settingPerGroup : settingsPerGroup) {
			String assetList = StringUtils.substringBetween(settingPerGroup, "[", "]");
			String[] assets = EMPTY_STRING_ARRAY;
			if (StringUtils.isNotBlank(assetList)) {
				assetList = assetList.trim();
				assets = StringUtils.split(assetList, ';');

				settingPerGroup = StringUtils.substringAfter(settingPerGroup, "]");
				if (settingPerGroup.isBlank()) {
					throw new IllegalConfigurationException("No allocation defined in property '" + propertyName + "' after asset symbols '" + assetList + "'");
				}
			}
			settingPerGroup = settingPerGroup.trim();
			T result = valueTransform.apply(settingPerGroup);
			consumer.accept(result, assets);
		}
	}

	public static int countDecimals(double d) {
		String tmp = new BigDecimal(d, new MathContext(8, RoundingMode.HALF_EVEN)).toPlainString();
		int decimals = 0;

		boolean in = false;
		boolean started = false;

		for (int i = 1; i < tmp.length(); i++) {
			if (tmp.charAt(i) == '.') {
				in = true;
			} else if (in) {
				if (tmp.charAt(i) != '0') {
					started = true;
				} else if (tmp.charAt(i) == '0' && decimals > 0 && started) {
					if (i + 1 < tmp.length() && tmp.charAt(i + 1) == '0') {
						return decimals;
					}
				}
				decimals++;
			}
		}

		return decimals;
	}
}


