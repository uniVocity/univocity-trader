package com.univocity.trader.importer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.candles.CandleRepository;
import com.univocity.trader.currency.Currencies;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.factory.UnivocityFactory;
import com.univocity.trader.indicators.base.TimeInterval;

public class MarketHistoryImporterRunner {
   public void run(String currenciesList) {
      final String[] currencies = Currencies.getInstance().fromList(currenciesList);
      final String[] pairs = Currencies.getInstance().makePairs(currencies, new String[] { "USDT" });
      final Exchange<Candle> exchange = UnivocityFactory.getInstance().getExchange(UnivocityFactory.getInstance().getUnivocityConfiguration().getExchangeClass());
      final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
      for (final String symbol : pairs) {
         CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
      }
   }
}
