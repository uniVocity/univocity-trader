package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.dynamic.code.*;

import javax.swing.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.function.*;

public class Argument {

	final String name;
	final Class<?> inputType;
	final double defaultValue;
	final double minimum;
	final double maximum;
	final double increment;

	private JComponent input;
	private Supplier<Object> componentGetter;
	private Consumer<Object> componentSetter;
	private boolean isUserCode;
	private UserCode<?> userCode;

	Argument(Parameter p) {
		this.inputType = p.getType();

		isUserCode = inputType.getAnnotation(FunctionalInterface.class) != null;
		Label label = p.getAnnotation(Label.class);
		if (label == null) {
			if (isUserCode) {
				this.name = "Configure input";
			} else {
				if (p.isNamePresent()) {
					this.name = p.getName();
				} else {
					this.name = p.getType().getSimpleName();
				}
			}
		} else {
			this.name = label.value();
		}

		if (p.getAnnotation(Default.class) != null) {
			Default defaults = p.getAnnotation(Default.class);
			this.defaultValue = defaults.value();
			this.maximum = defaults.maximum();
			this.increment = defaults.increment();
			this.minimum = defaults.minimum();
		} else if (p.getAnnotation(PositiveDefault.class) != null) {
			PositiveDefault defaults = p.getAnnotation(PositiveDefault.class);
			this.defaultValue = defaults.value();
			this.maximum = defaults.maximum();
			this.increment = defaults.increment();
			this.minimum = 1.0;
		} else {
			this.defaultValue = 0.0;
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
			} else if (isUserCode) {
				input = getSourceCodeInput(indicatorSelector);
			}
		}
		return input;
	}

	private JSpinner getFloatingPointInput(IndicatorSelector indicatorSelector) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMaximum(maximum);
		model.setMinimum(minimum);
		model.setValue(defaultValue);
		model.setStepSize(increment);
		JSpinner out = new JSpinner(model);
		componentGetter = out::getValue;
		componentSetter = out::setValue;
		out.addChangeListener(indicatorSelector.previewUpdater);
		return out;
	}

	private JSpinner getNumericInput(IndicatorSelector indicatorSelector) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMaximum((int) maximum);
		model.setMinimum((int) minimum);
		model.setValue((int) defaultValue);
		model.setStepSize((int) increment);
		JSpinner out = new JSpinner(model);
		componentGetter = out::getValue;
		componentSetter = out::setValue;
		out.addChangeListener(indicatorSelector.previewUpdater);
		return out;
	}

	private JCheckBox getBooleanInput(IndicatorSelector indicatorSelector) {
		JCheckBox out = new JCheckBox(name);
		componentGetter = out::isSelected;
		componentSetter = (v) -> out.setSelected(v == null ? false : (Boolean) v);
		out.addActionListener(indicatorSelector.previewUpdater);
		return out;
	}

	private Candle getTestCandle() {
		return new Candle(10, 10, 10.0, 20.0, 10.0, 10.0, 10.0);
	}

	private JButton getSourceCodeInput(IndicatorSelector indicatorSelector) {
		JButton out = new JButton("...");

		userCode = new UserCode<ToDoubleFunction<Candle>>(null);
		componentGetter = userCode::getSourceCode;
		componentSetter = (v) -> userCode.setSourceCode(String.valueOf(v));

		userCode.setSourceCode(UserCode.CANDLE_CLOSE);

		userCode.addCodeUpdateListener(indicatorSelector.previewUpdater);

		out.addActionListener((e) -> new UserCodeDialog(userCode).setVisible(true));

		return out;
	}

	public ArgumentValue getValue() {
		Object v = componentGetter == null ? null : componentGetter.get();
		if (isUserCode) {
			return new ArgumentValue(name, userCode.lastInstanceBuilt(), userCode.getLastWorkingVersion());
		}
		return new ArgumentValue(name, v);
	}

	void updateComponentValue(ArgumentValue argumentValue) {
		if (componentSetter != null && argumentValue != null) {
			Object value = argumentValue.getEditorValue();
			if(value == null && isUserCode){
				value = userCode.getSourceCode();
			}
			componentSetter.accept(value);
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
