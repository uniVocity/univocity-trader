package com.univocity.trader.account;

import com.univocity.trader.*;
import org.junit.*;

import static org.junit.Assert.*;

public class SimpleTradingFeesTest {

	@Test
	public void testBreakEvenCalculation(){
		TradingFees fees = SimpleTradingFees.percentage(1.0);

		double amount = fees.getBreakEvenAmount(100);
		assertEquals(101.99, amount, 0.01);

		double change = fees.getBreakEvenChange(100);
		assertEquals(1.99, change, 0.01);
	}
}