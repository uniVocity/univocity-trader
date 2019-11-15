package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;

import java.time.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketSimulation {

	public static void main(String... args) {

		//TODO: configure your database connection as needed.
		//DataSource ds = ?
		//CandleRepository.setDataSource(ds);

		MarketSimulator simulation = new MarketSimulator("USDT");
		simulation.tradeWith("BTC", "ADA", "LTC", "XRP", "ETH");

		simulation.strategies().add(ExampleStrategy::new);
		simulation.monitors().add(ExampleStrategyMonitor::new);

		simulation.setTradingFees(SimpleTradingFees.percentage(0.1));
		simulation.symbolInformation("ADAUSDT").minimumAssetsPerOrder(100.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
		simulation.symbolInformation("BTCUSDT").minimumAssetsPerOrder(0.001).priceDecimalPlaces(8).quantityDecimalPlaces(8);
		simulation.symbolInformation("LTCUSDT").minimumAssetsPerOrder(0.1).priceDecimalPlaces(8).quantityDecimalPlaces(8);
		simulation.symbolInformation("XRPUSDT").minimumAssetsPerOrder(50.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
		simulation.symbolInformation("ETHUSDT").minimumAssetsPerOrder(0.01).priceDecimalPlaces(8).quantityDecimalPlaces(8);

		simulation.account()
				.setAmount("USDT", 1000.0)
//				.maximumInvestmentPercentagePerAsset(30.0, "ADA", "ETH")
//				.maximumInvestmentPercentagePerAsset(50.0, "BTC", "LTC")
//				.maximumInvestmentAmountPerAsset(200, "XRP")
		;

		simulation.setSimulationStart(LocalDate.of(2018, 7, 1).atStartOfDay());
		simulation.setSimulationEnd(LocalDate.of(2019, 7, 1).atStartOfDay());

		simulation.listeners().add(new OrderExecutionToLog());
		SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
		simulation.listeners().add(stats);

		simulation.run();

		stats.printTradeStats();
	}
}
