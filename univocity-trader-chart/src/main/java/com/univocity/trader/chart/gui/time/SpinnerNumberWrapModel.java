package com.univocity.trader.chart.gui.time;

import javax.swing.*;

public class SpinnerNumberWrapModel extends SpinnerNumberModel {

	private final SpinnerWrapHelper helper;

	public SpinnerNumberWrapModel(JSpinner parent, int value, int minimum, int maximum, int stepSize) {
		super(value, minimum, maximum, stepSize);
		this.helper = new SpinnerWrapHelper(parent, listenerList);
	}

	public SpinnerNumberWrapModel addSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		helper.addSpinnerWrapListener(listener);
		return this;
	}

	public SpinnerNumberWrapModel removeSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		helper.removeSpinnerWrapListener(listener);
		return this;
	}

	@Override
	public Integer getValue() {
		return (Integer) super.getValue();
	}

	@Override
	public Integer getNextValue() {
		return helper.fire((Integer) super.getNextValue(), this.getMinimum());
	}

	@Override
	public Integer getPreviousValue() {
		return helper.fire((Integer) super.getPreviousValue(), this.getMaximum());
	}

	public Integer getMinimum() {
		return (Integer) super.getMinimum();
	}

	public Integer getMaximum() {
		return (Integer) super.getMaximum();
	}

	@Override
	public void setValue(Object v) {
		int value = (int) v;
		int oldValue = value;
		if (value < getMinimum()) {
			value = getMaximum();
		} else if (value > getMaximum()) {
			value = getMinimum();
		}
		super.setValue(value);

		if (oldValue != value) {
			helper.fireValueWrapped(new SpinnerNumberWrapEvent(helper.spinner, oldValue, value));
		}
	}
}
