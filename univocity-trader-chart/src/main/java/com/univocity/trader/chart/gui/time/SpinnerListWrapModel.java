package com.univocity.trader.chart.gui.time;

import org.apache.commons.lang3.*;

import javax.swing.*;

public class SpinnerListWrapModel<T> extends SpinnerListModel {

	private final SpinnerWrapHelper helper;
	private final T[] values;

	public SpinnerListWrapModel(JSpinner parent, T[] values, T value) {
		super(values);
		this.values = values;
		this.helper = new SpinnerWrapHelper(parent, listenerList);
		setValue(value);
	}

	public SpinnerListWrapModel<T> addSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		helper.addSpinnerWrapListener(listener);
		return this;
	}

	public SpinnerListWrapModel<T> removeSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		helper.removeSpinnerWrapListener(listener);
		return this;
	}

	public Integer getMinimum() {
		return 0;
	}

	public Integer getMaximum() {
		return values.length - 1;
	}

	@Override
	public T getNextValue() {
		return values[helper.fire(super.getNextValue() == null ? getMinimum() : ArrayUtils.indexOf(values, super.getNextValue()), this.getMinimum())];
	}

	@Override
	public T getPreviousValue() {
		return values[helper.fire(super.getPreviousValue() == null ? getMaximum() : ArrayUtils.indexOf(values, super.getPreviousValue()), this.getMaximum())];
	}

	@Override
	public void setValue(Object v) {
		int oldValue = ArrayUtils.indexOf(values, getValue());
		int value = ArrayUtils.indexOf(values, v);
		if (value < getMinimum()) {
			value = getMaximum();
		} else if (value > getMaximum()) {
			value = getMinimum();
		}
		super.setValue(values[value]);

		if (Math.abs(oldValue - value) == values.length - 1) {
			helper.fireValueWrapped(new SpinnerNumberWrapEvent(helper.spinner, oldValue, value));
		}
	}
}
