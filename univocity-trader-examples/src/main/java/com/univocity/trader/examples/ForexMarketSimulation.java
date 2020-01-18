package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;

import java.time.*;

import static com.univocity.trader.exchange.interactivebrokers.SecurityType.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ForexMarketSimulation {
	public static void main(String... args) {
		InteractiveBrokers.Simulator simulator = InteractiveBrokers.simulator();

//		TODO: configure your database connection as needed. By default MySQL will be used
//		simulator.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

		//you can test with one or more accounts at the same time
		Account account = simulator.configure().account();

		account
				.referenceCurrency("GBP") //Balances will be calculated using the reference currency.
				.tradeWith(FOREX, "EUR", "GBP");

		account
				.minimumInvestmentAmountPerTrade(500.0);

		account.strategies().add(ScalpingStrategy::new);
		account.monitors().add(ScalpingStrategyMonitor::new);

		account.listeners()
				.add(new OrderExecutionToLog())
				.add((symbol) -> new SimpleStrategyStatistics(symbol))
		;

		Simulation simulation = simulator.configure().simulation();
		simulation.initialFunds(1000.0)
				.tradingFees(SimpleTradingFees.percentage(0.0)) // NO FEE WARNING!!
				.fillOrdersOnPriceMatch()
				.simulateFrom(LocalDate.of(2019, 12, 24).atStartOfDay())
				.simulateTo(LocalDate.of(2020, 1, 10).atStartOfDay());

		simulator.symbolInformation("GBP").priceDecimalPlaces(5).quantityDecimalPlaces(2);
		simulator.symbolInformation("EUR").priceDecimalPlaces(5).quantityDecimalPlaces(2);

		//Interval of 1ms = REAL TIME TICKS
		simulator.configure().tickInterval(TimeInterval.millis(1));

//		execute simulation
//		simulator.run();

		simulation.backfillMonths(6);
		simulator.backfillHistory("EURGBP");
		System.exit(0);
	}
}
