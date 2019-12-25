package com.univocity.trader.examples;

import com.univocity.trader.config.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketSimulation {

	public static void main(String... args) {

//		TODO: configure your database connection as needed. The following options are available:

//		(a) Load configuration file
//		Configuration.load();                                //tries to open a univocity-trader.properties file
//		Configuration.loadFromCommandLine(args);		      //opens a file provided via the command line
//		Configuration.load("/path/to/config", "other.file"); //tries to find specific configuration files

//		(b) Configuration code
//		Configuration.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

//		(c) Use your own DataSource implementation:
//		DataSource ds = ?
//		CandleRepository.setDataSource(ds);

		Account account = Binance.load().account("jbax");
//		account
//				.referenceCurrency("USDT")
//				.tradeWith("BTC", "ADA", "LTC", "XRP", "ETH")
//				.minimumInvestmentAmountPerTrade(10.0)
//				.maximumInvestmentPercentagePerAsset(30.0, "ADA", "ETH")
//				.maximumInvestmentPercentagePerAsset(50.0, "BTC", "LTC")
//				.maximumInvestmentAmountPerAsset(200, "XRP")
		;

//		account.strategies().add(ExampleStrategy::new);
//		account.monitors().add(ExampleStrategyMonitor::new);
//
		SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
//
		account.listeners()
				.add(stats)
//				.add(new OrderExecutionToLog())
		;

		Simulation simulation = Binance.getInstance().simulation();
//		simulation.initialFunds(1000.0);
//		simulation.tradingFees(SimpleTradingFees.percentage(0.1));
//		simulation.simulationStart(LocalDate.of(2018, 7, 1).atStartOfDay());
//		simulation.simulationEnd(LocalDate.of(2019, 7, 1).atStartOfDay());

		MarketSimulator simulator = new MarketSimulator(account, simulation);

//		simulation.symbolInformation("ADAUSDT").minimumAssetsPerOrder(100.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
//		simulation.symbolInformation("BTCUSDT").minimumAssetsPerOrder(0.001).priceDecimalPlaces(8).quantityDecimalPlaces(8);
//		simulation.symbolInformation("LTCUSDT").minimumAssetsPerOrder(0.1).priceDecimalPlaces(8).quantityDecimalPlaces(8);
//		simulation.symbolInformation("XRPUSDT").minimumAssetsPerOrder(50.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
//		simulation.symbolInformation("ETHUSDT").minimumAssetsPerOrder(0.01).priceDecimalPlaces(8).quantityDecimalPlaces(8);


		simulator.run();

		stats.printTradeStats();
	}
}
