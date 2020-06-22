package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.annotation.*;

import javax.swing.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.function.*;

public class Argument {

	final String name;
	final Class<?> inputType;
	final double value;
	final double minimum;
	final double maximum;
	final double increment;

	private JComponent input;
	private Supplier<Object> valueGetter;

	Argument(Parameter p) {
		this.inputType = p.getType();
		Label label = p.getAnnotation(Label.class);
		if (label == null) {
			if (p.isNamePresent()) {
				this.name = p.getName();
			} else {
				this.name = p.getType().getSimpleName();
			}
		} else {
			this.name = label.value();
		}

		if (p.getAnnotation(Default.class) != null) {
			Default defaults = p.getAnnotation(Default.class);
			this.value = defaults.value();
			this.maximum = defaults.maximum();
			this.increment = defaults.increment();
			this.minimum = defaults.minimum();
		} else if (p.getAnnotation(PositiveDefault.class) != null) {
			PositiveDefault defaults = p.getAnnotation(PositiveDefault.class);
			this.value = defaults.value();
			this.maximum = defaults.maximum();
			this.increment = defaults.increment();
			this.minimum = 1.0;
		} else {
			this.value = 0.0;
			this.maximum = 0.0;
			this.increment = 0.0;
			this.minimum = 0.0;
		}
	}

	JComponent getComponent(IndicatorSelector indicatorSelector) {
		if (input == null) {
			if (inputType == double.class || inputType == Double.class || inputType == float.class || inputType == Float.class || inputType == BigDecimal.class) {
				input = getFloatingPointInput(indicatorSelector);
			} else if (inputType == long.class || inputType == Long.class || inputType == int.class || inputType == Integer.class || inputType == short.class || inputType == Short.class || inputType == byte.class || inputType == Byte.class) {
				input = getNumericInput(indicatorSelector);
			} else if (inputType == boolean.class || inputType == Boolean.class) {
				input = getBooleanInput(indicatorSelector);
			}
		}
		return input;
	}

	private JSpinner getFloatingPointInput(IndicatorSelector indicatorSelector) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMaximum(maximum);
		model.setMinimum(minimum);
		model.setValue(value);
		model.setStepSize(increment);
		JSpinner out = new JSpinner(model);
		valueGetter = out::getValue;
		out.addChangeListener(indicatorSelector.previewUpdater);
		return out;
	}

	private JSpinner getNumericInput(IndicatorSelector indicatorSelector) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMaximum((int) maximum);
		model.setMinimum((int) minimum);
		model.setValue((int) value);
		model.setStepSize((int) increment);
		JSpinner out = new JSpinner(model);
		valueGetter = out::getValue;
		out.addChangeListener(indicatorSelector.previewUpdater);
		return out;
	}

	private JCheckBox getBooleanInput(IndicatorSelector indicatorSelector) {
		JCheckBox out = new JCheckBox();
		valueGetter = out::isSelected;
		out.addActionListener(indicatorSelector.previewUpdater);
		return out;
	}

	public Object getValue() {
		return valueGetter == null ? null : valueGetter.get();
	}

	@Override
	public String toString() {
		return name;
	}
}
