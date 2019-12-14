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
import com.univocity.trader.currency.Currencies;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.exchange.ExchangeFactory;
import com.univocity.trader.exchange.binance.api.client.domain.market.Candlestick;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.notification.OrderExecutionToLog;
import com.univocity.trader.strategy.StrategyFactory;
import com.univocity.trader.utils.MailUtil;

public class LiveTraderRunner {
   public void run(String currenciesList) {
      final String[] currencies = Currencies.getInstance().fromList(currenciesList);
      final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      System.out.println("Strategy: " + univocityConfiguration.getStrategyClass().getName());
      final Exchange<Candlestick> exchange = ExchangeFactory.getInstance().getExchange(univocityConfiguration.getExchangeClass());
      LiveTrader<Candlestick> liveTrader = null;
      try {
         liveTrader = new LiveTrader<Candlestick>(exchange, TimeInterval.minutes(1), MailUtil.getEmailConfig());
         final String apiKey = univocityConfiguration.getExchangeAPIKey();
         final String secret = univocityConfiguration.getExchangeAPISecret();
         final Client<?> client = liveTrader.addClient(univocityConfiguration.getExchangeClientId(), ZoneId.systemDefault(), "USDT", apiKey, secret);
         client.tradeWith(currencies);
         client.strategies().add(StrategyFactory.getInstance().getStrategySupplier(univocityConfiguration.getStrategyClass()));
         for (final Class<?> clazz : univocityConfiguration.getStrategyMonitorClasses()) {
            System.out.println("Monitor: " + clazz.getName());
            client.monitors().add(StrategyFactory.getInstance().getStrategyMonitorSupplier(clazz));
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
