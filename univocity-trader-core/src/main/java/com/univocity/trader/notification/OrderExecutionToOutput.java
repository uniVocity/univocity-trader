package com.univocity.trader.notification;

import com.univocity.trader.account.*;

import java.io.*;

public class OrderExecutionToOutput extends OrderExecutionToLog {

	private final PrintStream outs;
	private final PrintWriter outw;

	public OrderExecutionToOutput() {
		this(System.out);
	}

	public OrderExecutionToOutput(OutputStream out) {
		this(new PrintStream(out));
	}

	public OrderExecutionToOutput(Writer out) {
		this.outw = new PrintWriter(out);
		this.outs = null;
	}

	public OrderExecutionToOutput(PrintStream out) {
		this.outs = out;
		this.outw = null;
	}

	protected void logDetails(Order order, Trade trade, Client client) {
		String row = generateDetailsRow(order, trade, client);
		if (outs != null) {
			outs.println(row);
		} else if (outw != null) {
			outw.println(row);
		}
	}
}
