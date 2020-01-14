package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import com.univocity.trader.candles.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class IBRequests {

	private static final Logger log = LoggerFactory.getLogger(IBRequests.class);
	private AtomicInteger requestId = new AtomicInteger(1);

	private final Queue<Candle> realTimeCandles = new ArrayBlockingQueue<>(10000);
	private final Queue<Candle> historicalCandles = new ArrayBlockingQueue<>(10000);

	private Map<Integer, Consumer<?>> pendingRequests = new ConcurrentHashMap<>();

	private final ResponseProcessor responseProcessor = new ResponseProcessor(realTimeCandles, historicalCandles, pendingRequests);

	private EJavaSignal signal = new EJavaSignal();
	private EClientSocket client = new EClientSocket(responseProcessor, signal);
	private EReader reader;
	private boolean ready = false;

	public IBRequests(String ip, int port, int clientID, String optionalCapabilities) {

		client.optionalCapabilities(optionalCapabilities);
		client.eConnect(ip, port, clientID);
		if (client.isConnected()) {
			log.info("Connected to TWS server (version {}})", client.serverVersion());
		} else {
			throw new IllegalStateException("Could not connect to TWS. Make sure it's running om " + (StringUtils.isBlank(ip) ? "localhost" : ip) + ":" + port);
		}

		reader = new EReader(client, signal);
		reader.start();

		new Thread(() -> {
			Thread.currentThread().setName("IB live stream");
			while (client.isConnected()) {
				ready = true;
				signal.waitForSignal();
				try {
					reader.processMsgs();
				} catch (Exception e) {
					log.error("Error processing messages", e);
				}
			}
		}).start();

		while (!ready) {
			Thread.yield();
		}
	}

	public void disconnect() {
		log.warn("Disconnecting from IB live stream");
		client.cancelRealTimeBars(3001);
		client.eDisconnect();
	}

	private int prepareRequest(Consumer<?> consumer) {
		int reqId = requestId.incrementAndGet();
		pendingRequests.put(reqId, consumer);
		return reqId;
	}

	public void searchForContract(Contract query, Consumer<SymbolInformation> resultConsumer) {
		client.reqContractDetails(prepareRequest(resultConsumer), query);
	}

	public void searchForex(String symbol, String currency) {
		Contract contract = new Contract();
		contract.symbol(symbol);
		contract.secType("CASH");
		contract.currency(currency);
		contract.exchange("IDEALPRO");

		client.reqContractDetails(requestId.incrementAndGet(), contract);

		//response goes to: ResponseProcessor.contractDetails(int reqId, ContractDetails contractDetails)
		//when no more contracts left: ResponseProcessor.contractDetailsEnd(int reqId)
	}

	public void searchStocks(String symbol, String currency) {
		Contract contract = new Contract();
		contract.symbol(symbol);
		contract.secType("STK");
		contract.currency(currency);
		//In the API side, NASDAQ is always defined as ISLAND
		contract.exchange("ISLAND");

	}

	public void searchForContracts(String symbolSearch) {
		client.reqMatchingSymbols(requestId.incrementAndGet(), symbolSearch);
	}

}
