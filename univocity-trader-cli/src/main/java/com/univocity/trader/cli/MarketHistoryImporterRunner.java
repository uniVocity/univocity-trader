package com.univocity.trader.cli;

import com.univocity.trader.exchange.binance.BinanceExchange;
import com.univocity.trader.markethistory.MarketHistoryUpdater;

public class MarketHistoryImporterRunner {
   public void run() {
      BinanceExchange exchange = new BinanceExchange();
      MarketHistoryUpdater marketHistoryUpdater = new MarketHistoryUpdater();
      marketHistoryUpdater.update(exchange, MarketHistoryUpdater.ALL_PAIRS);
   }
}