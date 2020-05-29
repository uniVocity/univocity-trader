package com.univocity.trader.chart.gui.components.time;

import java.util.*;

public class DateEditPaneEvent extends EventObject {

	private final Calendar oldDate;
	private final Calendar newDate;

	public DateEditPaneEvent(Object source, Calendar oldDate, Calendar newDate) {
		super(source);
		this.oldDate = oldDate;
		this.newDate = newDate;
	}

	public Calendar getOldDate() {
		return oldDate;
	}

	public Calendar getNewDate() {
		return newDate;
	}

}
