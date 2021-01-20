package com.univocity.trader.simulation.local;

import com.univocity.trader.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.*;

public final class Strategy implements EntryPoint {

	public static final class Simulator extends MarketSimulator<Configuration, SimulationAccount> {
		Simulator() {
			super(new Configuration(), () -> {
				throw new UnsupportedOperationException("A local simulation can't connect to a live exchange");
			});
		}
	}


	public static Simulator simulator() {
		return new Simulator();
	}

	public static LiveTrader<?, ?, ?> trader() {
		throw new UnsupportedOperationException("Live trading not supported by " + Simulator.class.getName());
	}
}