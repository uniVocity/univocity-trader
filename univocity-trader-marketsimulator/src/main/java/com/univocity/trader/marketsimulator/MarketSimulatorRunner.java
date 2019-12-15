package com.univocity.trader.marketsimulator;

import com.univocity.trader.account.SimpleTradingFees;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.currency.Currencies;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.notification.SimpleStrategyStatistics;
import com.univocity.trader.simulation.MarketSimulator;
import com.univocity.trader.strategy.StrategyFactory;

public class MarketSimulatorRunner {
   public void simulate(String currenciesList) {
      final String[] currencies = Currencies.getInstance().fromList(currenciesList);
      final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      System.out.println("Strategy: " + univocityConfiguration.getStrategyClass().getName());
      final MarketSimulator simulation = new MarketSimulator(univocityConfiguration.getSimulationReferenceCurrency());
      simulation.tradeWith(currencies);
      simulation.strategies().add(StrategyFactory.getInstance().getStrategySupplier(univocityConfiguration.getStrategyClass()));
      for (final Class<?> clazz : univocityConfiguration.getStrategyMonitorClasses()) {
         System.out.println("Monitor: " + clazz.getName());
         simulation.monitors().add(StrategyFactory.getInstance().getStrategyMonitorSupplier(clazz));
      }
      simulation.setTradingFees(SimpleTradingFees.percentage(0.1));
      // simulation.symbolInformation("ADAUSDT").minimumAssetsPerOrder(100.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
      // simulation.symbolInformation("BTCUSDT").minimumAssetsPerOrder(0.001).priceDecimalPlaces(8).quantityDecimalPlaces(8);
      // simulation.symbolInformation("LTCUSDT").minimumAssetsPerOrder(0.1).priceDecimalPlaces(8).quantityDecimalPlaces(8);
      // simulation.symbolInformation("XRPUSDT").minimumAssetsPerOrder(50.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
      // simulation.symbolInformation("ETHUSDT").minimumAssetsPerOrder(0.01).priceDecimalPlaces(8).quantityDecimalPlaces(8);
      simulation.account().setAmount(univocityConfiguration.getSimulationReferenceCurrency(), 1000.0).minimumInvestmentAmountPerTrade(10.0);
      // .maximumInvestmentPercentagePerAsset(30.0, "ADA", "ETH")
      // .maximumInvestmentPercentagePerAsset(50.0, "BTC", "LTC")
      // .maximumInvestmentAmountPerAsset(200, "XRP")
      ;
      simulation.setSimulationStart(univocityConfiguration.getSimulationStart());
      simulation.setSimulationEnd(univocityConfiguration.getSimulationEnd());
      simulation.listeners().add(new OrderExecutionToLog());
      final SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
      simulation.listeners().add(stats);
      simulation.run();
      stats.printTradeStats();
   }
}