package com.univocity.trader.markethistory;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.time.temporal.*;

public class MarketHistoryUpdater {
   public void update(Exchange exchange, String[] symbols) {
      final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
      for (String symbol : symbols) {
         CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
      }
   }
}
