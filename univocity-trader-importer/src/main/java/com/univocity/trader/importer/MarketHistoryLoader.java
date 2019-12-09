package com.univocity.trader.importer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.candles.CandleRepository;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.exchange.ExchangeFactory;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.utils.UnivocityConfiguration;

public class MarketHistoryLoader {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";
   /**
    * pairs
    */
   static String[][] ALL_PAIRS = new String[][] {
         // new String[]{"ADA", "USDT"}
         // , new String[]{"ALGO", "USDT"}
         // , new String[]{"ATOM", "USDT"}
         // , new String[]{"BAT", "USDT"}
         // , new String[]{"BCH", "USDT"}
         // , new String[]{"BNB", "USDT"},
         new String[] { "BTC", "USDT" }
         // , new String[]{"BTT", "USDT"}
         // , new String[]{"CELR", "USDT"}
         // , new String[]{"CHZ", "USDT"}
         // , new String[]{"CHZ", "USDT"}
         // , new String[]{"COCOS", "USDT"}
         // , new String[]{"DASH", "USDT"}
         // , new String[]{"DOCK", "USDT"}
         // , new String[]{"DUSK", "USDT"}
         // , new String[]{"ENJ", "USDT"}
         // , new String[]{"EOS", "USDT"}
         // , new String[]{"ERD", "USDT"}
         // , new String[]{"ETC", "USDT"}
         // , new String[]{"ETH", "USDT"}
         // , new String[]{"FET", "USDT"}
         // , new String[]{"HOT", "USDT"}
         // , new String[]{"ICX", "USDT"}
         // , new String[]{"IOST", "USDT"}
         // , new String[]{"IOTA", "USDT"}
         // , new String[]{"KAVA", "USDT"}
         // , new String[]{"KEY", "USDT"}
         // , new String[]{"LINK", "USDT"}
         // , new String[]{"LTC", "USDT"}
         // , new String[]{"MATIC", "USDT"}
         // , new String[]{"NANO", "USDT"}
         // , new String[]{"NEO", "USDT"}
         // , new String[]{"ONE", "USDT"}
         // , new String[]{"ONT", "USDT"}
         // , new String[]{"PERL", "USDT"}
         // , new String[]{"QTUM", "USDT"}
         // , new String[]{"TRX", "USDT"}
         // , new String[]{"VET", "USDT"}
         // , new String[]{"WAVES", "USDT"}
         // , new String[]{"WIN", "USDT"}
         // , new String[]{"XLM", "USDT"}
         // , new String[]{"XMR", "USDT"}
         // , new String[]{"XRP", "USDT"}
         // , new String[]{"XTZ", "USDT"}
         // , new String[]{"ZEC", "USDT"}
         // , new String[]{"ZIL", "USDT"}
         // , new String[]{"ZRX", "USDT"}
   };

   public static void main(String... args) {
      System.out.println("Univocity History Loader");
      /*
       * options
       */
      final Options options = new Options();
      final Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(true).desc("config file").build();
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
            UnivocityConfiguration.setConfigfileName(configFileName);
            final Exchange exchange = ExchangeFactory.getInstance().getExchange(UnivocityConfiguration.getInstance().getExchangeClass());
            final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
            for (final String[] pair : ALL_PAIRS) {
               final String symbol = pair[0] + pair[1];
               CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
            }
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}
