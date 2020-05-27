package com.univocity.trader.chart.gui.time;

import java.util.*;

public interface SpinnerNumberWrapListener extends EventListener {

	void valueWrappedToMinimum(SpinnerNumberWrapEvent e);

	void valueWrappedToMaximum(SpinnerNumberWrapEvent e);
}
