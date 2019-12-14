package com.univocity.trader.marketsimulator;

import java.time.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.account.SimpleTradingFees;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;
import com.univocity.trader.currency.Currencies;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.notification.SimpleStrategyStatistics;
import com.univocity.trader.simulation.MarketSimulator;
import com.univocity.trader.strategy.StrategyFactory;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketSimulatorMain {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";
   /**
    * currencies
    */
   private static final String CURRENCIES_OPTION = "currencies";

   public static void main(String... args) {
      System.out.println("Univocity Market Simulator");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(true).desc("config file").build();
      options.addOption(oo);
      oo = Option.builder().argName(CURRENCIES_OPTION).longOpt(CURRENCIES_OPTION).type(String.class).hasArg().required(true).desc("currencies").build();
      options.addOption(oo);
      /*
       * parse
       */
      final CommandLineParser parser = new DefaultParser();
      CommandLine cmd = null;
      try {
         cmd = parser.parse(options, args);
         /*
          * get the file
          */
         final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
         /*
          * get the currencies
          */
         final String currenciesList = cmd.getOptionValue(CURRENCIES_OPTION);
         if ((null != configFileName) && (null != currenciesList)) {
            final String[] currencies = Currencies.getInstance().fromList(currenciesList);
            ConfigFileUnivocityConfigurationImpl.setConfigfileName(configFileName);
            final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
            System.out.println("Strategy: " + univocityConfiguration.getStrategyClass().getName());
            final MarketSimulator simulation = new MarketSimulator("USDT");
            simulation.tradeWith(currencies);
            simulation.strategies().add(StrategyFactory.getInstance().getStrategySupplier(univocityConfiguration.getStrategyClass()));
            for (Class<?> clazz : univocityConfiguration.getStrategyMonitorClasses()) {
               System.out.println("Monitor: " + clazz.getName());
               simulation.monitors().add(StrategyFactory.getInstance().getStrategyMonitorSupplier(clazz));
            }
            simulation.setTradingFees(SimpleTradingFees.percentage(0.1));
            // simulation.symbolInformation("ADAUSDT").minimumAssetsPerOrder(100.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
            // simulation.symbolInformation("BTCUSDT").minimumAssetsPerOrder(0.001).priceDecimalPlaces(8).quantityDecimalPlaces(8);
            // simulation.symbolInformation("LTCUSDT").minimumAssetsPerOrder(0.1).priceDecimalPlaces(8).quantityDecimalPlaces(8);
            // simulation.symbolInformation("XRPUSDT").minimumAssetsPerOrder(50.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);
            // simulation.symbolInformation("ETHUSDT").minimumAssetsPerOrder(0.01).priceDecimalPlaces(8).quantityDecimalPlaces(8);
            simulation.account().setAmount("USDT", 1000.0).minimumInvestmentAmountPerTrade(10.0);
            // .maximumInvestmentPercentagePerAsset(30.0, "ADA", "ETH")
            // .maximumInvestmentPercentagePerAsset(50.0, "BTC", "LTC")
            // .maximumInvestmentAmountPerAsset(200, "XRP")
            ;
            simulation.setSimulationStart(LocalDate.of(2018, 7, 1).atStartOfDay());
            simulation.setSimulationEnd(LocalDate.of(2019, 7, 1).atStartOfDay());
            simulation.listeners().add(new OrderExecutionToLog());
            final SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
            simulation.listeners().add(stats);
            simulation.run();
            stats.printTradeStats();
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}