package com.univocity.trader.marketsimulator;

import com.univocity.trader.account.SimpleTradingFees;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.factory.UnivocityFactory;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.orderlistener.simplestrategystatistics.SimpleStrategyStatistics;
import com.univocity.trader.simulation.MarketSimulator;

public class MarketSimulatorRunner {
   public void simulate() {
      final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      System.out.println("Strategy:\t" + univocityConfiguration.getStrategyClass().getSimpleName());
      System.out.println("Ref Currency:\t" + univocityConfiguration.getExchangeReferenceCurrency());
      System.out.println("Currencies:");
      for (String c : univocityConfiguration.getExchangeCurrencies()) {
         System.out.println("\t\t" + c);
      }
      final MarketSimulator simulation = new MarketSimulator(univocityConfiguration.getSimulationReferenceCurrency());
      simulation.tradeWith(univocityConfiguration.getExchangeCurrencies());
      simulation.strategies().add(UnivocityFactory.getInstance().getStrategySupplier(univocityConfiguration.getStrategyClass()));
      System.out.println("Monitors:");
      for (final Class<?> clazz : univocityConfiguration.getStrategyMonitorClasses()) {
         System.out.println("\t\t" + clazz.getSimpleName());
         simulation.monitors().add(UnivocityFactory.getInstance().getStrategyMonitorSupplier(clazz));
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
      System.out.println("Start:\t\t" + univocityConfiguration.getSimulationStart().toString());
      System.out.println("End:\t\t" + univocityConfiguration.getSimulationEnd().toString());
      simulation.setSimulationStart(univocityConfiguration.getSimulationStart());
      simulation.setSimulationEnd(univocityConfiguration.getSimulationEnd());
      simulation.listeners().add(new OrderExecutionToLog());
      final SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
      simulation.listeners().add(stats);
      simulation.run();
      stats.printTradeStats();
   }
}
