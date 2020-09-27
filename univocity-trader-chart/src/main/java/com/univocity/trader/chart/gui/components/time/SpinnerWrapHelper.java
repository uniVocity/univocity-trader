package com.univocity.trader.chart.gui.components.time;

import javax.swing.*;
import javax.swing.event.*;

class SpinnerWrapHelper {

	final JSpinner spinner;
	final EventListenerList listenerList;

	SpinnerWrapHelper(JSpinner spinner, EventListenerList listenerList) {
		this.spinner = spinner;
		this.listenerList = listenerList;
	}

	void addSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		this.listenerList.add(SpinnerNumberWrapListener.class, listener);
	}

	void removeSpinnerWrapListener(SpinnerNumberWrapListener listener) {
		this.listenerList.remove(SpinnerNumberWrapListener.class, listener);
	}

	void fireValueWrapped(final SpinnerNumberWrapEvent e) {
		int result = e.getNewValue() - e.getOldValue();
		if (result < 0) {
			fireValueWrappedToMinimum(e);
		} else if (result > 0) {
			fireValueWrappedToMaximum(e);
		}
	}

	void fireValueWrappedToMinimum(SpinnerNumberWrapEvent e) {
		SpinnerNumberWrapListener[] listeners = listenerList.getListeners(SpinnerNumberWrapListener.class);
		for (SpinnerNumberWrapListener listener : listeners) {
			listener.valueWrappedToMinimum(e);
		}
	}

	void fireValueWrappedToMaximum(SpinnerNumberWrapEvent e) {
		SpinnerNumberWrapListener[] listeners = listenerList.getListeners(SpinnerNumberWrapListener.class);
		for (SpinnerNumberWrapListener listener : listeners) {
			listener.valueWrappedToMaximum(e);
		}
	}

	Integer fire(Integer value, Integer limit) {
		try {
			if (value == null) {
				fireValueWrapped(new SpinnerNumberWrapEvent(spinner, (int) spinner.getValue(), limit));
				return limit;
			}
			return value;
		} catch (Exception e) {
			return limit;
		}
	}
}
