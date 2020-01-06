package com.univocity.trader.examples.iqfeed;


import com.univocity.trader.iqfeed.*;

public class MarketHistoryLoader {
	public static void main(String... args) {
		// NOTE: configure your IQFeed connection prior to attempting connection
		// TODO: probably needs to
		IQFeed.Simulator simulator = IQFeed.simulator();
		simulator.configure().loadConfigurationFromProperties();

		simulator.configure().account()
				.tradeWith("GOOG");

		simulator.run();

		simulator
				.backfillHistory("GOOG");


	}
}