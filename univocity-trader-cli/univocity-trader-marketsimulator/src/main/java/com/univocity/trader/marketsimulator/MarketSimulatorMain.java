package com.univocity.trader.marketsimulator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;

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
            ConfigFileUnivocityConfigurationImpl.setConfigfileName(configFileName);
            final MarketSimulatorRunner marketSimulatorRunner = new MarketSimulatorRunner();
            marketSimulatorRunner.simulate(currenciesList);
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}