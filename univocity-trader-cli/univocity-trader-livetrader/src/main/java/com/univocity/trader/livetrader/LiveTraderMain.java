package com.univocity.trader.livetrader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;

class LiveTraderMain {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";

   public static void main(String... args) {
      System.out.println("Univocity Live Trader");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(true).desc("config file").build();
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
         if (null != configFileName) {
            ConfigFileUnivocityConfigurationImpl.setConfigfileName(configFileName);
            final LiveTraderRunner liveTraderRunner = new LiveTraderRunner();
            liveTraderRunner.run();
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}
