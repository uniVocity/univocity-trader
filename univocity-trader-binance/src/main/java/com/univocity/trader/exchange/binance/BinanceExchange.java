package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.domain.event.*;
import com.univocity.trader.exchange.binance.api.client.domain.general.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static com.univocity.trader.exchange.binance.api.client.domain.general.FilterType.*;

class BinanceExchange implements Exchange<Candlestick, Account> {

	private static final Logger log = LoggerFactory.getLogger(BinanceExchange.class);

	private BinanceApiWebSocketClient socketClient;
	private org.asynchttpclient.ws.WebSocket socketClientCloseable;
	private BinanceApiRestClient restClient;
	private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

	private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
	private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);
	private String listenKey;
	private Timer timer;
	private BinanceClientAccount binanceClientAccount;
	private char[] apiSecret;
	private String apiKey;
	private final double[] NO_PRICE = new double[]{-1.0};
	private boolean isTestNet = false;


	@Override
	public BinanceClientAccount connectToAccount(Account clientConfiguration) {
		this.apiKey = clientConfiguration.apiKey();
		this.apiSecret = clientConfiguration.secret();
		this.isTestNet = clientConfiguration.isTestNet();
		this.binanceClientAccount = new BinanceClientAccount(clientConfiguration.apiKey(), new String(clientConfiguration.secret()), this);
		return this.binanceClientAccount;
	}

	@Override
	public Candlestick getLatestTick(String symbol, TimeInterval interval) {
		List<Candlestick> candles = restClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval), 1, null, null);
		if (candles != null && candles.size() > 0) {
			return candles.get(0);
		}
		return null;
	}

	@Override
	public IncomingCandles<Candlestick> getLatestTicks(String symbol, TimeInterval interval) {
		try {
			return IncomingCandles.fromCollection(restClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval)));
		} catch (Exception e) {
			throw new IllegalStateException("Error returnning latest ticks of " + symbol, e);
		}
	}

	@Override
	public IncomingCandles<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		return IncomingCandles.fromCollection(restClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval), 1000, startTime, endTime));
	}

	@Override
	public PreciseCandle generatePreciseCandle(Candlestick exchangeCandle) {
		return new PreciseCandle(
				exchangeCandle.getOpenTime(),
				exchangeCandle.getCloseTime(),
				new BigDecimal(exchangeCandle.getOpen()),
				new BigDecimal(exchangeCandle.getHigh()),
				new BigDecimal(exchangeCandle.getLow()),
				new BigDecimal(exchangeCandle.getClose()),
				new BigDecimal(exchangeCandle.getVolume())
		);
	}

	@Override
	public void startKeepAlive(){
		new KeepAliveUserDataStream(restClient()).start();
	}
	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candlestick> consumer) {
		CandlestickInterval interval = CandlestickInterval.fromTimeInterval(tickInterval);
		log.info("Opening Binance {} live stream for: {}", tickInterval, symbols);
		socketClientCloseable = socketClient().onCandlestickEvent(symbols, interval, new BinanceApiCallback<>() {
			@Override
			public void onResponse(CandlestickEvent response) {
				try {
					priceReceived(response.getSymbol(), Double.parseDouble(response.getClose()));
				} catch (Exception e){
					log.warn("Error updating latest price of " + response.getSymbol(), e);
				}

				consumer.tickReceived(response.getSymbol(), response);
			}

			public void onFailure(Throwable cause) {
				consumer.streamError(cause);
			}

			public void onClose() {
				consumer.streamClosed();
			}
		});
	}

	@Override
	public void closeLiveStream() {
		if (socketClientCloseable != null) {
			socketClientCloseable.sendCloseFrame();
			socketClientCloseable = null;
		}
	}

	private final Map<String, double[]> latestPrices = new HashMap<>();

	private void priceReceived(String symbol, double price){
		latestPrices.compute(symbol, (s, v) -> {
			if (v == null) {
				return new double[]{price};
			} else {
				v[0] = price;
				return v;
			}
		});
	}

	@Override
	public Map<String, double[]> getLatestPrices() {
		try {
			List<TickerPrice> allPrices = restClient().getAllPrices();
			allPrices.forEach(ticker -> priceReceived(ticker.getSymbol(), ticker.getPriceAmount()));
		} catch (Exception e){
			log.warn("Unable to load latest prices from Binance", e);
		}
		return Collections.unmodifiableMap(latestPrices);
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		double price = latestPrices.getOrDefault(assetSymbol, NO_PRICE)[0];
		try {
			price = Double.parseDouble(restClient().getPrice(assetSymbol + fundSymbol).getPrice());
			priceReceived(assetSymbol + fundSymbol, price);
		} catch (Exception e) {
			log.error("Error getting latest price of " + assetSymbol + fundSymbol, e);
		}
		return price;
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		if (symbolInformation.isEmpty()) {
			Map<String, SymbolInfo> symbols = restClient().getExchangeInfo().getSymbols().stream().collect(Collectors.toMap(SymbolInfo::getSymbol, s -> s));

			symbols.forEach((symbol, symbolInfo) -> {
				SymbolFilter lotSize = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
				String step = lotSize.getStepSize(); //comes as: 0.01000000
				int quantityDecimalPlaces = step.indexOf('1') - 1;

				SymbolFilter tickSize = symbolInfo.getSymbolFilter(FilterType.PRICE_FILTER);
				String tickStep = tickSize.getTickSize(); //comes as: 0.01000000
				int priceDecimalPlaces = tickStep.indexOf('1') - 1;
				BigDecimal stepSize = new BigDecimal(step);

				SymbolFilter notional = symbolInfo.getSymbolFilter(MIN_NOTIONAL);
				BigDecimal minOrderAmount = new BigDecimal(notional.getMinNotional());

				SymbolInformation out = new SymbolInformation(symbol);
				out.quantityDecimalPlaces(quantityDecimalPlaces);
				out.priceDecimalPlaces(priceDecimalPlaces);
				out.minimumAssetsPerOrder(minOrderAmount);
				symbolInformation.put(symbol, out);
			});

		}
		return symbolInformation;
	}


	private BinanceApiWebSocketClient socketClient() {
		if (socketClient == null) {
			BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, apiSecret == null ? null : new String(apiSecret), asyncHttpClient, isTestNet);
			socketClient = factory.newWebSocketClient();
		}
		return socketClient;
	}



	private BinanceApiRestClient restClient() {
		if (restClient == null) {
			BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, apiSecret == null ? null : new String(apiSecret), asyncHttpClient, isTestNet);
		}
		return restClient;
	}

	@Override
	public int historicalCandleCountLimit() {
		return 1000;
	}

	//	@Override
//	public boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol) {
//		return symbolInformation.containsKey(currentAssetSymbol + targetAssetSymbol);
//	}
}
