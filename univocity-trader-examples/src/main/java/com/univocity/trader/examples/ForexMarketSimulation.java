package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;

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
		account.enableShorting();

		account
				.referenceCurrency("GBP") //Balances will be calculated using the reference currency.
				.tradeWith(FOREX, "EUR", "GBP");

		account
				.minimumInvestmentAmountPerTrade(100.0)
				.maximumInvestmentAmountPerTrade(100.0);


		//produces random signals.
		Strategy random = new Strategy() {
			@Override
			public Signal getSignal(Candle candle, Context context) {
				double v = Math.random();
				return v < 0.3 ? Signal.SELL : v > 0.7 ? Signal.BUY : Signal.NEUTRAL;
			}

			@Override
			public boolean exitOnOppositeSignal() {
				return false;
			}
		};

		account.strategies().add(() -> random);

		account.listeners()
				.add(new OrderExecutionToLog())
				.add((symbol) -> new SimpleStrategyStatistics(symbol))
		;

		Simulation simulation = simulator.configure().simulation();
		simulation.initialFunds(1000.0)
				.tradingFees(SimpleTradingFees.percentage(0.0)) // NO FEE WARNING!!
				.fillOrdersOnPriceMatch()
				.resumeBackfill(false)
				.simulateFrom("2019-10-10")
				.simulateTo("2019-10-15");

		simulator.symbolInformation("GBP").priceDecimalPlaces(5).quantityDecimalPlaces(2);
		simulator.symbolInformation("EURGBP").priceDecimalPlaces(5).quantityDecimalPlaces(2);

		//Interval of 1ms = REAL TIME TICKS
		simulator.configure().tickInterval(TimeInterval.millis(1));

		account.orderManager(new OrderManager() {
			@Override
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				order.attachToPriceChange(Order.Type.LIMIT, 0.025);
				order.attachToPriceChange(Order.Type.MARKET, -0.015);
			}
		});

//		execute simulation
		simulator.run();

//		simulator.backfillHistory("EURGBP");
	}
}
