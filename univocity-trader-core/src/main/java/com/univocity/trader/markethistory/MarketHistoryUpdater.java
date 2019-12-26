package com.univocity.trader.markethistory;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.time.temporal.*;

public class MarketHistoryUpdater {
   public void update(Exchange exchange, String[] symbols) {
      CandleRepository candleRepository = new CandleRepository(new DatabaseConfiguration());
      final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
      for (String symbol : symbols) {
         candleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
      }
   }
}
