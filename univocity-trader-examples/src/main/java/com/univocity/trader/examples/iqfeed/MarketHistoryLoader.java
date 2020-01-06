package com.univocity.trader.examples.iqfeed;


import com.univocity.trader.iqfeed.*;

public class MarketHistoryLoader {
	public static void main(String... args) {
		IQFeed.Simulator simulator = IQFeed.simulator();

		// NOTE: configure your IQFeed connection prior to attempting connection
		//OPTIONAL - loads full or partial configuration from "iqfeed.properties" file
		simulator.configure().loadConfigurationFromProperties();

		//Modifies any configured value in the "iqfeed.properties" file. Or configure everything via code
		simulator.configure().account()
//				.iqPortalPath("/path/to/iq/portal")
//				.product("PRODUCT")
//				.version("1.0")
//				.host("host")
//				.port(0)
//				.login("Your Login")
//				.pass("Your password")
				.tradeWith("GOOG");

		simulator.run();

		simulator
				.backfillHistory("GOOG");


	}
}