package com.univocity.trader.simulation;

import ch.qos.logback.classic.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import org.junit.*;

import static ch.qos.logback.classic.Level.*;
import static org.slf4j.Logger.*;
import static org.slf4j.LoggerFactory.*;

public class MockExchangeTest {

	private static Strategy strategy() {
		return new Strategy() {
			@Override
			public Signal getSignal(Candle candle) {
				return candle.close > 5 ? Signal.SELL : Signal.BUY;
			}
		};
	}

	@Test
	@Ignore
	public void testMockExchange() throws Exception{
		((Logger) getLogger(ROOT_LOGGER_NAME)).setLevel(TRACE);
		MockExchange.Trader trader = MockExchange.trader();

		trader.configure().account()
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "USDT")
				.strategies()
				.add(MockExchangeTest::strategy);

		Trader[] t = new Trader[1];
		OrderManager om = new DefaultOrderManager(){
			@Override
			public TimeInterval getOrderUpdateFrequency() {
				return TimeInterval.millis(1);
			}

			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Trader trader) {
				super.prepareOrder(priceDetails, book, order, trader);
				t[0] = trader;
			}
		};
		trader.configure().account().orderManager(om);

		OrderExecutionToCsv csv = new OrderExecutionToCsv("debug");
		trader.configure().account().listeners().add(()->csv);

		trader.run();

		Thread.sleep(1500);

		csv.simulationEnded(t[0], null);


	}

}
