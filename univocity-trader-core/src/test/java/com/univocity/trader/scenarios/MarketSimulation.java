package com.univocity.trader.scenarios;

import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.simulation.local.*;
import com.univocity.trader.utils.*;
import org.junit.*;

import java.io.*;
import java.time.*;

import static org.junit.Assert.*;

public class MarketSimulation {

	static final String input = "" +
			"OPEN_TIME,CLOSE_TIME,OPEN,HIGH,LOW,CLOSE,VOLUME,SIGNAL\n" +
			"1532217540000,1532217599999,83.44,83.44,83.39,83.44,62.93009,BUY\n" +
			"1532217600000,1532217659999,83.42,83.43,83.35,83.41,129.28858,SELL\n" +
			"1532217660000,1532217719999,83.35,83.43,83.13,83.13,239.99897,BUY\n" +
			"1532217720000,1532217779999,83.15,83.25,83.01,83.01,160.75216,SELL\n" +
			"1532218080000,1532218139999,83.01,83.11,83.01,83.11,59.31481,BUY\n" +
			"1532218140000,1532218199999,83.12,83.18,83.07,83.07,125.24617,SELL\n" +
			"1532218200000,1532218259999,83.06,83.23,83.06,83.23,143.53221,BUY\n" +
			"1532218260000,1532218319999,83.18,83.24,83.05,83.05,108.6254,SELL\n" +
			"1532218320000,1532218379999,83.18,83.23,83.14,83.22,65.8686,BUY\n" +
			"1532218380000,1532218439999,83.06,83.23,83.06,83.13,53.93096,SELL\n" +
			"1532218500000,1532218559999,83.28,83.32,83.22,83.31,103.97525,BUY\n" +
			"1532218560000,1532218619999,83.23,83.35,83.23,83.35,39.01225,SELL\n" +
			"1532218620000,1532218679999,83.28,83.37,83.27,83.36,48.5318,BUY\n" +
			"1532218680000,1532218739999,83.37,83.42,83.34,83.42,187.20029,SELL\n" +
			"1532218740000,1532218799999,83.37,83.48,83.34,83.38,309.5456,BUY\n" +
			"1532227380000,1532227439999,83.21,83.21,83.06,83.06,319.33566,SELL\n" +
			"1532227440000,1532227499999,83.04,83.15,83.0,83.15,368.0443,BUY\n";

	@Test
	public void testMarketOrders(){
		Strategy.Simulator simulator = Strategy.simulator();
		//simulator.getCandleRepository()
		SimulationAccount account = simulator.configure().account();

		account
				.referenceCurrency("USDT")
				.tradeWith("LTC")
				.maximumInvestmentPercentagePerAsset(50.0)
				.minimumInvestmentAmountPerTrade(25.0)
		;

		account.strategies().add(() -> new SignalReproducer("LTCUSDT", new StringReader(input)));

		account.orderManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				order.setType(Order.Type.MARKET);
			}
		});

		simulator.configure().account().listeners().add(new OrderExecutionToLog());
		simulator.configure().account().listeners().add(new OrderListener() {
			@Override
			public void simulationEnded(Trader trader, Client client) {
				assertEquals(1225.636493, trader.totalFundsInReferenceCurrency(), 6);
			}
		});


		Simulation simulation = simulator.configure().simulation();
		simulation.initialFunds(1000.0)
				.tradingFees(SimpleTradingFees.percentage(0.1))
				.fillOrdersImmediately()
				.simulateFrom(LocalDate.of(2018, 7, 1).atStartOfDay())
				.simulateTo(LocalDate.of(2019, 7, 1).atStartOfDay());

		simulator.symbolInformation("USDT").priceDecimalPlaces(2).quantityDecimalPlaces(2);

//		execute simulation
		simulator.run();

	}
}