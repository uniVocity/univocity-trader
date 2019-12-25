package com.univocity.trader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.Exchange;
import com.univocity.trader.config.Configuration;
import com.univocity.trader.config.Simulation;
import com.univocity.trader.exchange.binance.Account;
import com.univocity.trader.exchange.binance.Binance;
import com.univocity.trader.exchange.binance.BinanceExchange;
import com.univocity.trader.markethistory.MarketHistoryUpdater;
import com.univocity.trader.notification.SimpleStrategyStatistics;
import com.univocity.trader.simulation.MarketSimulator;

public class Main {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";
   /**
    * account option
    */
   private static final String ACCOUNT_OPTION = "account";
   /**
    * import option
    */
   private static final String IMPORT_OPTION = "import";
   /**
    * simulate option
    */
   private static final String SIMULATE_OPTION = "simulate";
   /**
    * livetrade option
    */
   private static final String LIVETRADE_OPTION = "livetrade";

   private static void livetrade(Exchange exchange, Account account) {
      // TODO
   }

   public static void main(String... args) {
      System.out.println("Univocity CLI");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(false).desc("config file").build();
      options.addOption(oo);
      oo = Option.builder().argName(ACCOUNT_OPTION).longOpt(ACCOUNT_OPTION).type(String.class).hasArg().required(false).desc("account").build();
      options.addOption(oo);
      oo = Option.builder().argName(IMPORT_OPTION).longOpt(IMPORT_OPTION).hasArg(false).required(false).desc("import").build();
      options.addOption(oo);
      oo = Option.builder().argName(SIMULATE_OPTION).longOpt(SIMULATE_OPTION).hasArg(false).required(false).desc("simulate").build();
      options.addOption(oo);
      oo = Option.builder().argName(LIVETRADE_OPTION).longOpt(LIVETRADE_OPTION).hasArg(false).required(false).desc("live trade").build();
      options.addOption(oo);
      /*
       * parse
       */
      final CommandLineParser parser = new DefaultParser();
      CommandLine cmd = null;
      try {
         cmd = parser.parse(options, args);
         /*
          * config
          */
         final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
         if (null != configFileName) {
            Configuration.load(configFileName);
         } else {
            Configuration.load();
         }
         /*
          * TODO we might want to fix this....
          */
         final Exchange exchange = new BinanceExchange();
         final String accountName = cmd.getOptionValue(ACCOUNT_OPTION);
         Account account = null;
         if (null != accountName) {
            account = Binance.load(CONFIG_OPTION).account(accountName);
         }
         /*
          * run command
          */
         if (cmd.hasOption(IMPORT_OPTION)) {
            /*
             * update market history
             */
            updateMarketHistory(exchange);
         } else if (cmd.hasOption(SIMULATE_OPTION)) {
            /*
             * simulate
             */
            simulate(exchange, account);
         } else if (cmd.hasOption(LIVETRADE_OPTION)) {
            /*
             * live trade
             */
            livetrade(exchange, account);
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }

   private static void simulate(Exchange exchange, Account account) {
      final SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
      account.listeners().add(stats);
      final Simulation simulation = Binance.getInstance().simulation();
      final MarketSimulator simulator = new MarketSimulator(account, simulation);
      simulator.run();
      stats.printTradeStats();
   }

   private static void updateMarketHistory(Exchange exchange) {
      final MarketHistoryUpdater marketHistoryUpdater = new MarketHistoryUpdater();
      marketHistoryUpdater.update(exchange, MarketHistoryUpdater.ALL_PAIRS);
   }
}