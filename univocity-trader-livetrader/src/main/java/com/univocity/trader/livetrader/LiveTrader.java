package com.univocity.trader.livetrader;

import java.math.BigDecimal;
import java.time.ZoneId;

import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.Client;
import com.univocity.trader.account.DefaultOrderManager;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.exchange.binance.BinanceTrader;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.notification.MailSenderConfig;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.strategy.example.ExampleStrategy;
import com.univocity.trader.strategy.example.ExampleStrategyMonitor;

public class LiveTrader {
   private static final MailSenderConfig getEmailConfig() {
      MailSenderConfig out = new MailSenderConfig();
      out.setReplyToAddress("dev@univocity.com");
      out.setSmtpHost("smtp.gmail.com");
      out.setSmtpTlsSsl(true);
      out.setSmtpPort(587);
      out.setSmtpUsername("<YOU>@gmail.com");
      out.setSmtpPassword("<YOUR SMTP PASSWORD>".toCharArray());
      out.setSmtpSender("<YOU>>@gmail.com");
      return out;
   }

   public static void main(String... args) {
      // TODO: configure your database connection as needed.
      // DataSource ds = ?
      // CandleRepository.setDataSource(ds);
      BinanceTrader binance = new BinanceTrader(TimeInterval.minutes(1), getEmailConfig());
      String apiKey = "<YOUR BINANCE API KEY>";
      String secret = "<YOUR BINANCE API SECRET>";
      Client client = binance.addClient("<YOUR E-MAIL>", ZoneId.systemDefault(), "USDT", apiKey, secret);
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
}
