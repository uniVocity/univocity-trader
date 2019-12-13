package com.univocity.trader.livetrader;

import java.math.BigDecimal;
import java.time.ZoneId;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.LiveTrader;
import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.Client;
import com.univocity.trader.account.DefaultOrderManager;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.exchange.ExchangeFactory;
import com.univocity.trader.exchange.binance.api.client.domain.market.Candlestick;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.strategy.example.ExampleStrategy;
import com.univocity.trader.strategy.example.ExampleStrategyMonitor;
import com.univocity.trader.utils.MailUtil;
import com.univocity.trader.utils.Symbol;

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
            final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
            ConfigFileUnivocityConfigurationImpl.setConfigfileName(configFileName);
            final Exchange<Candlestick> exchange = ExchangeFactory.getInstance().getExchange(univocityConfiguration.getExchangeClass());
            LiveTrader<Candlestick> binance = null;
            try {
               binance = new LiveTrader<Candlestick>(exchange, TimeInterval.minutes(1), MailUtil.getEmailConfig());
               final String apiKey = univocityConfiguration.getExchangeAPIKey();
               final String secret = univocityConfiguration.getExchangeAPISecret();
               final Client client = binance.addClient(univocityConfiguration.getExchangeClientId(), ZoneId.systemDefault(), "USDT", apiKey, secret);
               client.tradeWith(Symbol.BTC.name(), Symbol.ETH.name(), Symbol.XRP.name(), Symbol.ADA.name());
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
            } finally {
               binance.close();
            }
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}
