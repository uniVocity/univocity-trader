package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import org.slf4j.*;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class LiveTrader<T> implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(LiveTrader.class);

	private List<Client<T>> clients = new ArrayList<>();

	private String allClientPairs;
	private final Map<String, Long> symbols = new ConcurrentHashMap<>();
	private final Exchange<T> api;
	private final TimeInterval tickInterval;
	private final SmtpMailSender mailSender;
	private long lastHour;
	private Map<String, String[]> allPairs;

	private class PollThread extends Thread {
		public PollThread() {
			setName("candle poller");
		}

		public void run() {
			while (true) {
				try {
					long now = System.currentTimeMillis();
					if (now - lastHour > HOUR.ms) {
						lastHour = System.currentTimeMillis();
						log.info("Updating balances");
						clients.forEach(Client::updateBalances);
					}

					int[] count = new int[]{0};

					symbols.forEach((symbol, lastUpdate) -> {
						if (lastUpdate == null || (now - lastUpdate) > tickInterval.ms) {
							count[0]++;
							try {
								log.info("Polling next candle for {} as we didn't get an update since {}", symbol, lastUpdate == null ? "N/A" : Candle.getFormattedDateTimeWithYear(lastUpdate));
								T tick = api.getLatestTick(symbol, tickInterval);
								if (tick != null) {
									symbols.put(symbol, now);
									clients.parallelStream().forEach(c -> c.processCandle(symbol, tick, false));
								}
							} catch (Exception e) {
								TimeInterval waitTime = api.handlePollingException(symbol, e);
								if (waitTime != null) {
									LiveTrader.sleep(waitTime.ms);
								}
							}
							LiveTrader.sleep(500);
						}
					});

					if (count[0] == symbols.size()) { //all symbols being polled.
						log.info("Websocket seems to be offline, trying to start it up");
						retryRunWebsocket();
					}

					LiveTrader.sleep(5_000);
				} catch (Exception e) {
					log.error("Error polling Candles", e);
				}

			}
			//List<Candle> Candles = client.getCandlestickBars("NEOETH", CandlestickInterval.ONE_MINUTE, 1, null, null);
		}
	}

	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			log.error("Thread polling interrupted", e);
		}
	}

	public LiveTrader(Exchange<T> api, TimeInterval tickInterval, MailSenderConfig mailSenderConfig) {
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));
		this.api = api;
		this.tickInterval = tickInterval;
		this.mailSender = mailSenderConfig == null ? null : new SmtpMailSender(mailSenderConfig);
	}

	private void initialize() {
		if (allPairs == null) {
			allPairs = new TreeMap<>();
			for (Client client : clients) {
				client.initialize(api, mailSender);
				allPairs.putAll(client.getSymbolPairs());
			}
		}
		updateDatabase();
	}

	private void updateDatabase() {
		if (allClientPairs != null) {
			return;
		}
		StringBuilder tmp = new StringBuilder();
		for (String symbol : allPairs.keySet()) {
			if (tmp.length() > 0) {
				tmp.append(',');
			}
			tmp.append(symbol);
		}
		this.allClientPairs = tmp.toString().toLowerCase();

		//fill history with last 30 days of data
		for (String symbol : allPairs.keySet()) {
			CandleRepository.fillHistoryGaps(api, symbol, Instant.now().minus(30, ChronoUnit.DAYS), tickInterval);
		}

		//quick update for the last 30 minutes in case the previous step takes too long and we miss a few ticks
		for (String symbol : allPairs.keySet()) {
			CandleRepository.fillHistoryGaps(api, symbol, Instant.now().minus(30, ChronoUnit.MINUTES), tickInterval);
			symbols.put(symbol, System.currentTimeMillis());
		}

		//loads last 30 day history of every symbol to initialize indicators (such as moving averages et al) in a useful state
		for (String symbol : allPairs.keySet()) {
			Enumeration<Candle> it = CandleRepository.iterate(symbol, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now(), false);
			while (it.hasMoreElements()) {
				Candle candle = it.nextElement();
				if (candle != null) {
					clients.forEach(c -> c.processCandle(symbol, candle, true));
				}
			}
		}

		//loads the very latest ticks and process them before we can finally connect to the live stream and trade for real.
		for (String symbol : allPairs.keySet()) {
			List<T> candles = api.getLatestTicks(symbol, tickInterval);
			for (T candle : candles) {
				clients.forEach(c -> c.processCandle(symbol, candle, true));
			}
		}
	}

	private AtomicInteger retryCount = new AtomicInteger(0);

	public void run() {
		initialize();
		runLiveStream();
	}


	private void runLiveStream() {
		new Thread(() -> {
			log.debug("Starting web socket. Retry count: {}", retryCount);
			if (retryCount.get() > 0) {
				try {
					close();
				} catch (Exception e) {
					log.error("Error closing socket", e);
				}
			} else {
				new PollThread().start();
			}

			api.openLiveStream(allClientPairs, tickInterval, new TickConsumer<T>() {
				@Override
				public void tickReceived(String symbol, T tick) {
					long now = System.currentTimeMillis();
					symbols.put(symbol, now);
					clients.forEach(c -> c.processCandle(symbol, tick, false));
				}

				@Override
				public void streamError(Throwable cause) {
					log.error("Error listening to candle events, reconnecting...", cause);
					retryRunWebsocket();
				}

				@Override
				public void streamClosed() {
					retryRunWebsocket();
				}
			});
		}).start();

		if (retryCount.get() == 0) {
			clients.forEach(c -> {
				c.updateBalances();
				c.sendBalanceEmail("Trading robot started. Here is your current position.");
			});
		}
	}

	private void retryRunWebsocket() {
		retryCount.incrementAndGet();
		runLiveStream();
	}

	@Override
	public void close() {
		try {
			if (api != null) {
				try {
					api.closeLiveStream();
				} catch (Exception e) {
					log.error("Error closing socket client connection", e);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public Client addClient(String email, ZoneId timezone, String referenceCurrencySymbol, String apiKey, String secret) {
		ClientAccount clientApi = api.connectToAccount(apiKey, secret);
		Client client = new Client(email, timezone, referenceCurrencySymbol, clientApi);
		clients.add(client);
		return client;
	}
}
