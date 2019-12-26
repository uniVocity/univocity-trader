package com.univocity.trader.tickers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.univocity.parsers.common.NormalizedString;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;

/**
 * @author tom@khubla.com
 */
public class Tickers {
   /**
    * instance
    */
   private static Tickers instance = null;

   public static Tickers getInstance() {
      if (null == instance) {
         instance = new Tickers();
      }
      return instance;
   }

   /**
    * datafile
    */
   private final String DATAFILE_NAME = "/tickers.csv";
   private final Map<NormalizedString, Ticker> tickers = new ConcurrentHashMap<>();

   private Tickers() {
      read();
   }

   private void read() {
      tickers.clear();
      CsvParserSettings settings = new CsvParserSettings();
      settings.setLineSeparatorDetectionEnabled(true);
      settings.setReadInputOnSeparateThread(false);
      for (Ticker ticker : new CsvRoutines(settings).iterate(Ticker.class, Tickers.class.getResourceAsStream(DATAFILE_NAME), "UTF-8")) {
         tickers.put(NormalizedString.valueOf(ticker.getSymbol()), ticker);
      }
   }

   public Ticker find(String name) {
      return this.tickers.get(NormalizedString.valueOf(name));
   }

   public int size() {
      return this.tickers.size();
   }

   public String[] makePairs(String[] assetSymbols, String[] fundSymbols) {
      String[] ret = new String[assetSymbols.length * fundSymbols.length];
      int idx = 0;
      for (int i = 0; i < assetSymbols.length; i++) {
         for (int j = 0; j < fundSymbols.length; j++) {
            ret[idx++] = getSymbol(assetSymbols[i]) + getSymbol(fundSymbols[j]);
         }
      }
      return ret;
   }

   public String[] getSymbols(Ticker.Type type) {
      List<String> lst = new ArrayList<String>();
      for (Ticker ticker : tickers.values()) {
         if (ticker.getType() == type) {
            lst.add(ticker.getSymbol());
         }
      }
      String[] ret = new String[lst.size()];
      lst.toArray(ret);
      return ret;
   }

   private String getSymbol(String name) {
      Ticker currency = find(name);
      if (currency == null) {
         throw new IllegalArgumentException("Unknown ticker: " + name + " . Available tickers are: " + new TreeSet<>(tickers.keySet()));
      }
      return currency.getSymbol();
   }
}