package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.exchange.binance.BinanceExchange;
import com.univocity.trader.markethistory.MarketHistoryUpdater;

public class MarketHistoryLoader {
   public static void main(String... args) {
      // TODO: configure your database connection as needed. The following options are available:
      // (a) Load configuration file
      // Configuration.load(); //tries to open a univocity-trader.properties file
      // Configuration.loadFromCommandLine(args); //opens a file provided via the command line
      // Configuration.load("/path/to/config", "other.file"); //tries to find specific configuration files
      // (b) Configuration code
      // Configuration.configure().database()
      // .jdbcDriver("my.database.DriverClass")
      // .jdbcUrl("jdbc:mydb://localhost:5555/database")
      // .user("admin")
      // .password("qwerty");
      // (c) Use your own DataSource implementation:
      // DataSource ds = ?
      // CandleRepository.setDataSource(ds);
      BinanceExchange exchange = new BinanceExchange();
      MarketHistoryUpdater marketHistoryUpdater = new MarketHistoryUpdater();
      marketHistoryUpdater.update(exchange, MarketHistoryUpdater.ALL_PAIRS);
      System.exit(0);
   }
}
