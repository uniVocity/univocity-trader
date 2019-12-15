package com.univocity.trader.livetrader;

import java.math.BigDecimal;
import java.time.ZoneId;

import com.univocity.trader.LiveTrader;
import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.Client;
import com.univocity.trader.account.DefaultOrderManager;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.exchange.binance.api.client.domain.market.Candlestick;
import com.univocity.trader.factory.UnivocityFactory;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.notification.OrderExecutionToLog;

public class LiveTraderRunner {
   public void run() {
      final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      System.out.println("Strategy: " + univocityConfiguration.getStrategyClass().getSimpleName());
      final Exchange<Candlestick> exchange = UnivocityFactory.getInstance().getExchange(univocityConfiguration.getExchangeClass());
      LiveTrader<Candlestick> liveTrader = null;
      try {
         liveTrader = new LiveTrader<Candlestick>(exchange, TimeInterval.minutes(1), UnivocityFactory.getInstance().getEmailConfig());
         final String apiKey = univocityConfiguration.getExchangeAPIKey();
         final String secret = univocityConfiguration.getExchangeAPISecret();
         final Client<?> client = liveTrader.addClient(univocityConfiguration.getExchangeClientId(), ZoneId.systemDefault(), univocityConfiguration.getExchangeReferenceCurrency(), apiKey, secret);
         client.tradeWith(univocityConfiguration.getExchangeCurrencies());
         client.strategies().add(UnivocityFactory.getInstance().getStrategySupplier(univocityConfiguration.getStrategyClass()));
         for (final Class<?> clazz : univocityConfiguration.getStrategyMonitorClasses()) {
            System.out.println("Monitor: " + clazz.getSimpleName());
            client.monitors().add(UnivocityFactory.getInstance().getStrategyMonitorSupplier(clazz));
         }
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
         liveTrader.run();
      } finally {
         liveTrader.close();
      }
   }
}
