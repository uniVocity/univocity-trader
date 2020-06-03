package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.lang.reflect.*;
import java.util.*;

class IndicatorDefinition implements Comparable<IndicatorDefinition>{

	private final String indicator;
	final Map<String, Class<?>> argumentTypes = new LinkedHashMap<>();
	private final Parameter[] parameters;

	private final Method factoryMethod;
	private final Constructor<?> constructor;
	private final String description;

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Method factoryMethod) {
		this(indicatorType, null, factoryMethod, factoryMethod.getParameters());
	}

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Constructor<? extends Indicator> constructor) {
		this(indicatorType, constructor, null, constructor.getParameters());
	}

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Constructor<? extends Indicator> constructor, Method factoryMethod, Parameter[] params) {
		this.indicator = indicatorType.getSimpleName();
		this.constructor = constructor;
		this.factoryMethod = factoryMethod;
		this.parameters = params;

		for (Parameter p : params) {
			if (p.getType() != TimeInterval.class) {
				if(p.isNamePresent()) {
					argumentTypes.put(p.getName(), p.getType());
				} else {
					argumentTypes.put(p.getType().getSimpleName(), p.getType());
				}
			}
		}

		description = indicator + (argumentTypes.isEmpty() ? "" : " (" + String.join(", ", argumentTypes.keySet()) + ")");
	}

	public Set<String> argumentNames() {
		return argumentTypes.keySet();
	}

	public Indicator create(Map<String, Object> arguments, TimeInterval interval) {
		try {
			Object[] args = new Object[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				Parameter param = parameters[i];
				if (param.getType() == TimeInterval.class) {
					args[i] = interval;
				} else {
					if(param.isNamePresent()) {
						args[i] = arguments.get(param.getName());
					} else {
						args[i] = arguments.get(param.getType().getSimpleName());
					}
				}
			}
			Indicator indicator;
			if (factoryMethod != null) {
				indicator = (Indicator) factoryMethod.invoke(null, args);
			} else {
				indicator = (Indicator) constructor.newInstance(args);
			}
			return indicator;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public int compareTo(IndicatorDefinition o) {
		return this.toString().compareTo(o.toString());
	}

	public static List<IndicatorDefinition> loadConstructors(Class<? extends Indicator> indicatorClass) {
		List<IndicatorDefinition> out = new ArrayList<>();

		for (Constructor c : indicatorClass.getConstructors()) {
			out.add(new IndicatorDefinition(indicatorClass, c));
		}
		return out;
	}

	public static List<IndicatorDefinition> loadIndicators(Class<?> factoryClass) {
		List<IndicatorDefinition> out = new ArrayList<>();

		Method[] methods = factoryClass.getMethods();
		for (Method m : methods) {
			Class<?> returnType = m.getReturnType();
			if (Indicator.class.isAssignableFrom(returnType)) {
				out.add(new IndicatorDefinition((Class<? extends Indicator>) returnType, m));
			}
		}

		return out;
	}
}
