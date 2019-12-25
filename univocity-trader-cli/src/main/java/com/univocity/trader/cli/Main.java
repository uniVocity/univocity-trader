package com.univocity.trader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.config.Configuration;
import com.univocity.trader.exchange.binance.BinanceExchange;
import com.univocity.trader.markethistory.MarketHistoryUpdater;

public class Main {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";
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
   /**
    * we might want to fix this....
    */
   private static BinanceExchange exchange = new BinanceExchange();

   public static void main(String... args) {
      System.out.println("Univocity CLI");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(false).desc("config file").build();
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
         if (cmd.hasOption(IMPORT_OPTION)) {
            /*
             * update market history
             */
            updateMarketHistory();
         } else if (cmd.hasOption(SIMULATE_OPTION)) {
            /*
             * simulate
             */
            simulate();
         } else if (cmd.hasOption(LIVETRADE_OPTION)) {
            /*
             * live trade
             */
            livetrade();
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }

   private static void updateMarketHistory() {
      MarketHistoryUpdater marketHistoryUpdater = new MarketHistoryUpdater();
      marketHistoryUpdater.update(exchange, MarketHistoryUpdater.ALL_PAIRS);
   }

   private static void simulate() {
   }

   private static void livetrade() {
   }
}