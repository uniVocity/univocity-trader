package com.univocity.trader.chart.gui.time;

import javax.swing.*;
import java.util.*;

public class SpinnerNumberWrapEvent extends EventObject {

	private final int oldValue;
	private final int newValue;

	public SpinnerNumberWrapEvent(Object source, int oldValue, int newValue) {
		super(source);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public JSpinner getSource() {
		return (JSpinner) super.getSource();
	}

	public int getOldValue() {
		return oldValue;
	}

	public int getNewValue() {
		return newValue;
	}

}
