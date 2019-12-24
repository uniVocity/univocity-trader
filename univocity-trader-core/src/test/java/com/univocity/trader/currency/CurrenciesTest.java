package com.univocity.trader.currency;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

public class CurrenciesTest {
   @Test
   public void testCurrencies() {
      Currencies currencies = Currencies.getInstance();
      assertTrue(currencies.size() >= 72);
   }
}