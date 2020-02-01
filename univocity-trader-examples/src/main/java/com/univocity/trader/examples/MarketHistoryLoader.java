package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.notification.*;

import java.time.*;

/**
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketHistoryLoader {
	public static void main(String... args) {
		Binance.Simulator simulator = Binance.simulator();

//		TODO: configure your database connection as needed. By default MySQL will be used
//		simulator.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

		Simulation simulation = simulator.configure().simulation();

//		configure to update historical data in database going back 2 years from today.
		simulation.backfillYears(2);

//		Pulls any missing candlesticks from the exchange and store them in our local database.
//      It runs over stored candles backwards and will try to fill any gaps until the date 2 years ago from today is reached.
		simulator.backfillHistory("BTCUSDT", "ADAUSDT");

		System.exit(0);
	}
}
