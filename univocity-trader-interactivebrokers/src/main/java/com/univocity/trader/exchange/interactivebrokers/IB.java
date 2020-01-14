package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import java.util.*;


class IB implements Exchange<Candle, Account> {

	private static final Logger log = LoggerFactory.getLogger(IB.class);

	private final EWrapper eWrapper = new EWrapperImpl();
	private EJavaSignal signal = new EJavaSignal();
	private EClientSocket client = new EClientSocket(eWrapper, signal);
	private EReader reader;

	IB() {
		this("", 7497, 0, "");
	}

	IB(String ip, int port, int clientID, String optionalCapabilities) {
		// connect to TWS
		client.optionalCapabilities(optionalCapabilities);
		client.eConnect(ip, port, clientID);
		if (client.isConnected()) {
			log.info("Connected to Tws server version " + client.serverVersion() + " at " + client.getTwsConnectionTime());
		}
	}

	@Override
	public IBAccount connectToAccount(Account clientConfiguration) {
		return new IBAccount(this);
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {
		return null;
	}

	@Override
	public List<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		return null;
	}

	@Override
	public List<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		return null;
	}

	@Override
	public Candle generateCandle(Candle exchangeCandle) {
		return null;
	}

	@Override
	public PreciseCandle generatePreciseCandle(Candle exchangeCandle) {
		return null;
	}

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		reader = new EReader(client, signal);
		reader.start();

		new Thread(() -> {
			while (client.isConnected()) {
				signal.waitForSignal();
				try {
					reader.processMsgs();
				} catch (Exception e) {
					log.error("Error processing messages", e);
				}
			}
		}).start();
	}

	@Override
	public void closeLiveStream() throws Exception {
		log.warn("Disconnecting from IB live stream");
		client.eDisconnect();
	}

	@Override
	public Map<String, Double> getLatestPrices() {
		return null;
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		return null;
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return 0;
	}

	@Override
	public TimeInterval handlePollingException(String symbol, Exception e) {
		return null;
	}
}
