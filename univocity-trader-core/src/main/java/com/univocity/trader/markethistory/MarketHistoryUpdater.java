package com.univocity.trader.markethistory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import com.univocity.trader.Exchange;
import com.univocity.trader.candles.CandleRepository;
import com.univocity.trader.indicators.base.TimeInterval;

public class MarketHistoryUpdater {
   public void update(Exchange exchange, String[] symbols) {
      final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
      for (String symbol : symbols) {
         CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
      }
   }
}
