package com.univocity.trader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.config.Configuration;

public class Main {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";

   public static void main(String... args) {
      System.out.println("Univocity CLI");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(false).desc("config file").build();
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
          * update market history
          */
         final MarketHistoryImporterRunner marketHistoryImporterRunner = new MarketHistoryImporterRunner();
         marketHistoryImporterRunner.run();
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}