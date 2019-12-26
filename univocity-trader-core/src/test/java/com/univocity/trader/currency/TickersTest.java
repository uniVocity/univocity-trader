package com.univocity.trader.currency;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

import com.univocity.trader.tickers.Tickers;

public class TickersTest {
   @Test
   public void testTickersLoad() {
      Tickers tickers = Tickers.getInstance();
      assertTrue(tickers.size() >= 72);
   }
}