package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class LiveTrader<T, C extends Configuration<C, A>, A extends AccountConfiguration<A>> implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(LiveTrader.class);

	private List<Client<T>> clients = new ArrayList<>();

	private String allClientPairs;
	private final Map<String, Long> symbols = new ConcurrentHashMap<>();
	private final Exchange<T, A> exchange;
	private TimeInterval tickInterval;
	private SmtpMailSender mailSender;
	private long lastHour;
	private Map<String, String[]> allPairs;
	private C configuration;
	private DatabaseCandleRepository candleRepository;

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
								T tick = exchange.getLatestTick(symbol, tickInterval);
								if (tick != null) {
									symbols.put(symbol, now);
									clients.parallelStream().forEach(c -> c.processCandle(symbol, tick, false));
								}
							} catch (Exception e) {
								TimeInterval waitTime = exchange.handlePollingException(symbol, e);
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

	public LiveTrader(Exchange<T, A> exchange, C configuration) {
		this.configuration = configuration;
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));
		this.exchange = exchange;

	}

	private final SmtpMailSender mailSender() {
		if (mailSender == null) {
			EmailConfiguration mail = configuration.mailSender();
			this.mailSender = mail.isConfigured() ? new SmtpMailSender(mail) : null;
		}
		return mailSender;
	}

	public C configure() {
		return configuration;
	}

	public DatabaseCandleRepository candleRepository() {
		if (candleRepository == null) {
			candleRepository = new DatabaseCandleRepository(configuration.database());
		}
		return candleRepository;
	}

	private void initialize() {
		this.tickInterval = configuration.tickInterval();
		SignalRepository signalRepository = configuration.signalRepositoryDir() == null ? null : new SignalRepository(configuration.signalRepositoryDir());

		if (clients.isEmpty()) {
			for (var account : configuration.accounts()) {
				ClientAccount clientAccount = exchange.connectToAccount(account);
				var client = new Client<T>(createAccountManager(clientAccount, account, () -> signalRepository));
				clients.add(client);
			}
		}

		if (allPairs == null) {
			allPairs = new TreeMap<>();
			for (Client client : clients) {
				client.initialize(candleRepository(), exchange, mailSender());
				allPairs.putAll(client.getAllSymbolPairs());
			}
		}

		updateDatabase();
	}

	protected AccountManager createAccountManager(ClientAccount clientAccount, A account, Supplier<SignalRepository> signalRepository) {
		return new AccountManager(clientAccount, account, signalRepository);
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
		CandleHistoryBackfill backfill = new CandleHistoryBackfill(candleRepository());

		this.allClientPairs = tmp.toString().toLowerCase();

		if (configuration.updateHistoryBeforeLiveTrading()) {
			//fill history with last 30 days of data
			for (String symbol : allPairs.keySet()) {
				backfill.fillHistoryGaps(exchange, symbol, Instant.now().minus(30, ChronoUnit.DAYS), tickInterval);
			}

			//quick update for the last 30 minutes in case the previous step takes too long and we miss a few ticks
			for (String symbol : allPairs.keySet()) {
				backfill.fillHistoryGaps(exchange, symbol, Instant.now().minus(30, ChronoUnit.MINUTES), tickInterval);
				symbols.put(symbol, System.currentTimeMillis());
			}

			//loads last 60 day history of every symbol to initialize indicators (such as moving averages et al) in a useful state
			for (String symbol : allPairs.keySet()) {
				Period warmUpPeriod = configuration.warmUpPeriod();
				Instant warmUpStart = Instant.now();
				if (warmUpPeriod != null) {
					warmUpStart = warmUpStart.minus(warmUpPeriod);
				} else {
					warmUpStart = warmUpStart.minus(60, ChronoUnit.DAYS);
				}

				Enumeration<Candle> it = candleRepository().iterate(symbol, warmUpStart, Instant.now(), false);
				while (it.hasMoreElements()) {
					Candle candle = it.nextElement();
					if (candle != null) {
						clients.forEach(c -> c.processCandle(symbol, candle, true));
					}
				}
			}

			//loads the very latest ticks and process them before we can finally connect to the live stream and trade for real.
			for (String symbol : allPairs.keySet()) {
				IncomingCandles<T> candles = exchange.getLatestTicks(symbol, tickInterval);
				for (T candle : candles) {
					clients.forEach(c -> c.processCandle(symbol, candle, true));
				}
			}
		}
	}

	private AtomicInteger retryCount = new AtomicInteger(0);

	public void run() {
		initialize();
		runLiveStream();
		runKeepAlive();
	}

	private void runKeepAlive() {
		exchange.startKeepAlive();
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
				if (configuration.pollCandles()) {
					new PollThread().start();
				}
			}

			exchange.openLiveStream(allClientPairs, tickInterval, new TickConsumer<T>() {
				@Override
				public void tickReceived(String s, T tick) {
					String symbol = s.trim().toUpperCase();
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
		try {
			close();
		} finally {
			retryCount.incrementAndGet();
			runLiveStream();
		}
	}

	public Exchange<?, ?> exchange() {
		return exchange;
	}

	@Override
	public void close() {
		try {
			if (exchange != null) {
				try {
					exchange.closeLiveStream();
				} catch (Exception e) {
					log.error("Error closing socket client connection", e);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
