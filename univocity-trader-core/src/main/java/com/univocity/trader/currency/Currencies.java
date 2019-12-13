package com.univocity.trader.currency;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.opencsv.CSVReader;

/**
 * @author tom@khubla.com
 */
public class Currencies {
   /**
    * instance
    */
   private static Currencies instance = null;

   public static Currencies getInstance() {
      if (null == instance) {
         instance = new Currencies();
      }
      return instance;
   }

   /**
    * datafile
    */
   private final String DATAFILE_NAME = "/currencies.csv";
   /**
    * map of objects
    */
   private final HashMap<String, Currency> currencies = new HashMap<String, Currency>();

   private Currencies() {
      read();
   }

   private void read() {
      CSVReader csvReader = null;
      try {
         final InputStream inputStream = Currencies.class.getResourceAsStream(DATAFILE_NAME);
         final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         csvReader = new CSVReader(inputStreamReader);
         String[] line;
         while ((line = csvReader.readNext()) != null) {
            if (line.length >= 2) {
               if (line.length == 2) {
                  currencies.put(line[0].trim(), new Currency(line[0], Currency.CurrencyType.valueOf(line[1].trim()), null));
               } else {
                  currencies.put(line[0].trim(), new Currency(line[0], Currency.CurrencyType.valueOf(line[1].trim()), line[2].trim()));
               }
            }
         }
      } catch (final IOException e) {
         e.printStackTrace();
      } finally {
         try {
            csvReader.close();
         } catch (final IOException e) {
            e.printStackTrace();
         }
      }
   }

   public Currency find(String name) {
      return this.currencies.get(name);
   }

   public int size() {
      return this.currencies.size();
   }

   public String[] fromList(String currenciesList) {
      String[] cl = currenciesList.split(",");
      for (int i = 0; i < cl.length; i++) {
         String symbol = cl[i].trim();
         Currency currency = this.find(symbol);
         if (null == currency) {
            throw new IllegalStateException();
         } else {
            cl[i] = symbol;
         }
      }
      return cl;
   }

   public String[] makePairs(String[] currencies1, String[] currencies2) {
      String[] ret = new String[currencies1.length * currencies2.length];
      int idx = 0;
      for (int i = 0; i < currencies1.length; i++) {
         for (int j = 0; j < currencies2.length; j++) {
            ret[idx++] = currencies1[i] + currencies2[j];
         }
      }
      return ret;
   }
}
