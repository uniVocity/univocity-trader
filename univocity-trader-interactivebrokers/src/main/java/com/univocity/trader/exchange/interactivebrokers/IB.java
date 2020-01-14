package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.interactivebrokers.api.*;
import com.univocity.trader.indicators.base.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;


class IB implements Exchange<Candle, Account> {

	private static final Logger log = LoggerFactory.getLogger(IB.class);

	private final Queue<Candle> realTimeCandles = new ArrayBlockingQueue<>(10000);
	private final Queue<Candle> historicalCandles = new ArrayBlockingQueue<>(10000);

	private final ResponseProcessor responseProcessor = new ResponseProcessor(realTimeCandles, historicalCandles);

	private EJavaSignal signal = new EJavaSignal();
	private EClientSocket client = new EClientSocket(responseProcessor, signal);
	private EReader reader;

	IB() {
		this("", 7497, 0, "");
	}

	IB(String ip, int port, int clientID, String optionalCapabilities) {
		// connect to TWS
		client.optionalCapabilities(optionalCapabilities);
		client.eConnect(ip, port, clientID);
		if (client.isConnected()) {
			log.info("Connected to TWS server (version {}})", client.serverVersion());
		} else {
			throw new IllegalStateException("Could not connect to TWS. Make sure it's running om " + (StringUtils.isBlank(ip) ? "localhost" : ip) + ":" + port);
		}
	}

	@Override
	public IBAccount connectToAccount(Account clientConfiguration) {
		return new IBAccount(this);
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {

		Contract contract = new Contract();
//		m_contract.conid( m_conId.getInt() );
		contract.symbol(symbol.toUpperCase());
//		m_contract.secType( m_secType.getSelectedItem() );
//		m_contract.lastTradeDateOrContractMonth( m_lastTradeDateOrContractMonth.getText() );
//		m_contract.strike( m_strike.getDouble() );
//		m_contract.right( m_right.getSelectedItem() );
//		m_contract.multiplier( m_multiplier.getText() );
		contract.exchange("SMART");
//		m_contract.primaryExch( compExch);
		contract.currency("USD");
//		m_contract.localSymbol( m_localSymbol.getText().toUpperCase() );
//		m_contract.tradingClass( m_tradingClass.getText().toUpperCase() );


		client.reqRealTimeBars(3001, contract, 5, "MIDPOINT", true, null);

		while (client.isConnected() && realTimeCandles.size() == 0) {
			try {
				Thread.sleep(5000);
				log.debug("No " + symbol + " ticks after 5 seconds");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		client.cancelRealTimeBars(3001);

		return realTimeCandles.poll();
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
			Thread.currentThread().setName("IB live stream");
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
		client.cancelRealTimeBars(3001);
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

	//TODO: remove this once implementation is `f`inalized
	public static void main(String... args) throws Exception {
		IB ib = new IB();
		System.out.println(ib.getLatestTick("GOOG", TimeInterval.seconds(5)));
		System.exit(0);
	}
}
