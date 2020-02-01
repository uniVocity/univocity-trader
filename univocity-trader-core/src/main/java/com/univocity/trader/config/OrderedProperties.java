package com.univocity.trader.config;

import java.util.*;

/**
 * An override over {@link java.util.Properties} to return properties in the
 * order they are defined in the file.
 *
 * @author Univocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class OrderedProperties extends Properties {

	private static final long serialVersionUID = 4335376309848461398L;

	private final ArrayList<Object> propertySequence;

	/**
	 * Creates a new instances of this ordered {@code Properties} object.
	 */
	public OrderedProperties() {
		propertySequence = new ArrayList<Object>();
	}

	@Override
	public Enumeration<?> propertyNames() {
		return Collections.enumeration(propertySequence);
	}

	@Override
	public Object put(Object key, Object value) {
		propertySequence.remove(key);
		propertySequence.add(key);
		return super.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		propertySequence.remove(key);
		return super.remove(key);
	}

}