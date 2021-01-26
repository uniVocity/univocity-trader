package com.univocity.trader.simulation;

import com.univocity.parsers.csv.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.local.*;
import org.junit.*;

import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import static org.junit.Assert.*;

public class FileBasedSimulation {

	public static RowFormat<String, CsvParserSettings> csvFileFormat() {
		return RowFormat.csv()
				.selectColumnsByName()
				.dateAndTimePattern("yyyy-MM-dd")
				.openDateTime("Date")
				.noCloseDateTime()
				.openingPrice("Open")
				.highestPrice("High")
				.lowestPrice("Low")
				.closingPrice("Adj Close")
				.volume("Volume")
				.build();
	}

	public static Path pathToRepositoryDir() {
		URL dataDir = FileBasedSimulation.class.getResource("data");
		if (dataDir == null) {
			dataDir = FileBasedSimulation.class.getResource("/data");
		}
		try {
			return Paths.get(dataDir.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void testFileBasedSimulation() {

		Strategy.Simulator simulator = Strategy.simulator();

		simulator.configure()
				.fileRepository() //run simulation against local files.
				.dir(pathToRepositoryDir()) //path to a directory containing data in tabular text files (CSV, TSV or fixed-width)
				.csv() //format of the files in the directory given above.
				.selectColumnsByName() //files have a header row to identify column names
				.dateAndTimePattern("yyyy-MM-dd") //format of date date/time columns
				.openDateTime("Date") //name of column for opening date/time in files
				.noCloseDateTime() // candle information in the files we're processing don't have a closing date/time
				.openingPrice("Open") // name of the column for the candle's opening price
				.highestPrice("High") // name of the column for the candle's high price
				.lowestPrice("Low")// name of the column for the candle's low price
				.closingPrice("Adj Close")// name of the column for the candle's closing price
				.volume("Volume")// name of the column for the candle's volume
				.build(); //builds a RowFormat instance

		SimulationAccount account = simulator.configure().account();
		account
				.referenceCurrency("USD")
				.tradeWith("BTC")
				.maximumInvestmentAmountPerTrade(250.0)
				.minimumInvestmentAmountPerTrade(10);
		;

		Balance[] btc = new Balance[1];
		Balance[] usd = new Balance[1];
		List<Order> orders = new ArrayList<>();

		OrderListener orderListener = new OrderListener() {
			@Override
			public void orderSubmitted(Order order, Trade trade, Client client) {
				orders.add(order);
				System.out.println(order);
			}

			@Override
			public void simulationEnded(Trader trader, Client client) {
				btc[0] = trader.balanceOf("BTC");
				usd[0] = trader.balanceOf("USD");
			}
		};

		account.strategies().add(() -> (candle, context) -> Signal.BUY);
		account.listeners().add(orderListener);

		Simulation simulation = simulator.configure().simulation();
		simulation.initialFunds(1000.0)
				.tradingFees(SimpleTradingFees.percentage(0.1))
				.fillOrdersImmediately()
				.simulateFrom(LocalDate.of(2020, 5, 1).atStartOfDay())
				.simulateTo(LocalDate.of(2020, 12, 1).atStartOfDay());

		simulator.symbolInformation("USD").priceDecimalPlaces(2).quantityDecimalPlaces(2);

		simulator.run();

		assertEquals("USD(0.10099990)", usd[0].toString());
		assertEquals("BTC(0.11160632)", btc[0].toString());
		assertEquals(4, orders.size());

	}
}
