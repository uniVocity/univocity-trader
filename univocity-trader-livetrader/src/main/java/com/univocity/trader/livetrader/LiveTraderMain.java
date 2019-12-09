package com.univocity.trader.livetrader;

import java.math.BigDecimal;
import java.time.ZoneId;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.Client;
import com.univocity.trader.account.DefaultOrderManager;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.exchange.binance.BinanceTrader;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.strategy.example.ExampleStrategy;
import com.univocity.trader.strategy.example.ExampleStrategyMonitor;
import com.univocity.trader.utils.MailUtil;
import com.univocity.trader.utils.UnivocityConfiguration;

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
            final BinanceTrader binance = new BinanceTrader(TimeInterval.minutes(1), MailUtil.getEmailConfig());
            final UnivocityConfiguration univocityConfiguration = UnivocityConfiguration.getInstance();
            final String apiKey = univocityConfiguration.getExchangeAPIKey();
            final String secret = univocityConfiguration.getExchangeAPISecret();
            final Client client = binance.addClient("<YOUR E-MAIL>", ZoneId.systemDefault(), "USDT", apiKey, secret);
            client.tradeWith("BTC", "ETH", "XRP", "ADA");
            client.strategies().add(ExampleStrategy::new);
            client.monitors().add(ExampleStrategyMonitor::new);
            client.account().maximumInvestmentAmountPerAsset(20);
            client.account().setOrderManager(new DefaultOrderManager() {
               @Override
               public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
                  switch (order.getSide()) {
                     case BUY:
                        order.setPrice(order.getPrice().multiply(new BigDecimal("0.9"))); // 10% less
                        break;
                     case SELL:
                        order.setPrice(order.getPrice().multiply(new BigDecimal("1.1"))); // 10% more
                  }
               }
            });
            client.listeners().add(new OrderExecutionToLog());
            binance.run();
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}
