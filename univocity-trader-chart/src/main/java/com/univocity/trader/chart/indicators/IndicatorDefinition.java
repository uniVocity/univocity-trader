package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

class IndicatorDefinition implements Comparable<IndicatorDefinition> {

	final List<Argument> arguments = new ArrayList<>();
	private final Method factoryMethod;
	private final Constructor<?> constructor;
	private final String description;
	final Render[] renders;
	final boolean overlay;
	final double min;
	final double max;
	Compose compose;

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Method factoryMethod) {
		this(indicatorType, null, factoryMethod, factoryMethod.getParameters());
	}

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Constructor<? extends Indicator> constructor) {
		this(indicatorType, constructor, null, constructor.getParameters());
	}

	private IndicatorDefinition(Class<? extends Indicator> indicatorType, Constructor<? extends Indicator> constructor, Method factoryMethod, Parameter[] params) {
		this.constructor = constructor;
		this.factoryMethod = factoryMethod;

		Overlay overlay = null;
		Underlay underlay = null;
		Render[] rendererList = null;
		if (factoryMethod != null) {
			rendererList = factoryMethod.getAnnotationsByType(Render.class);
			overlay = factoryMethod.getAnnotation(Overlay.class);
			underlay = factoryMethod.getAnnotation(Underlay.class);
			compose = factoryMethod.getAnnotation(Compose.class);
		} else if (constructor != null) {
			rendererList = indicatorType.getAnnotationsByType(Render.class);
			overlay = indicatorType.getAnnotation(Overlay.class);
			underlay = indicatorType.getAnnotation(Underlay.class);
			compose = indicatorType.getAnnotation(Compose.class);
		}

		if (compose != null) {
			rendererList = compose.elements();
		} else {
			if (rendererList == null || rendererList.length == 0) {
				rendererList = indicatorType.getAnnotationsByType(Render.class);
			}
		}

		min = underlay == null ? Double.MIN_VALUE : underlay.min();
		max = underlay == null ? Double.MAX_VALUE : underlay.max();

		renders = rendererList;

		StringBuilder tmp = new StringBuilder();
		for (Parameter p : params) {
			Argument a = new Argument(p);
			arguments.add(a);

			if (a.inputType != TimeInterval.class) {
				if (tmp.length() > 0) {
					tmp.append(", ");
				} else {
					tmp.append('(');
				}
				tmp.append(a.name);
			}
		}

		if (tmp.length() > 0) {
			tmp.append(')');
		}

		String displayName = overlay != null ? overlay.label() : underlay != null ? underlay.label() : "";
		if (displayName.isBlank()) {
			displayName = indicatorType.getSimpleName();
		}
		description = displayName + tmp.toString();
		this.overlay = overlay != null;
	}

	public ArgumentValue[] getArgumentValues(Supplier<TimeInterval> interval) {
		ArgumentValue[] args = new ArgumentValue[arguments.size()];
		try {
			for (int i = 0; i < arguments.size(); i++) {
				Argument arg = arguments.get(i);
				if (arg.inputType == TimeInterval.class) {
					args[i] = new ArgumentValue(arg.name, interval);
				} else {
					args[i] = arg.getValue();
				}
			}
			return args;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating " + this + " with: " + Arrays.toString(args), e);
		}
	}

	Indicator create(ArgumentValue[] argumentValues) {
		Object[] args = new Object[argumentValues.length];
		try {
			for (int i = 0; i < argumentValues.length; i++) {
				args[i] = argumentValues[i].getValue();
			}
			Indicator indicator;
			if (factoryMethod != null) {
				indicator = (Indicator) factoryMethod.invoke(null, args);
			} else {
				indicator = (Indicator) constructor.newInstance(args);
			}
			return indicator;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating " + this + " with: " + Arrays.toString(args), e);
		}
	}

	public void setEditorValues(ArgumentValue[] argumentValues){
		if(argumentValues != null){
			for (int i = 0; i < argumentValues.length; i++) {
				this.arguments.get(i).updateComponentValue(argumentValues[i]);
			}
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
