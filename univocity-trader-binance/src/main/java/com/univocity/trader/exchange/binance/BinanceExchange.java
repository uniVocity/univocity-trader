package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.domain.event.*;
import com.univocity.trader.exchange.binance.api.client.domain.general.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.math.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static com.univocity.trader.exchange.binance.api.client.domain.general.FilterType.*;

public class BinanceExchange implements Exchange<Candlestick> {

	private static final Logger log = LoggerFactory.getLogger(BinanceExchange.class);

	private BinanceApiWebSocketClient socketClient;
	private org.asynchttpclient.ws.WebSocket socketClientCloseable;
	private BinanceApiRestClient restClient;
	private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

	private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
	private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);


	@Override
	public BinanceClientAccount connectToAccount(String apiKey, String secret) {
		return new BinanceClientAccount(apiKey, secret, this);
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
	public List<Candlestick> getLatestTicks(String symbol, TimeInterval interval) {
		return restClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval));
	}

	@Override
	public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		return restClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval), 1000, startTime, endTime);
	}

	@Override
	public Candle generateCandle(Candlestick exchangeCandle) {
		return new Candle(
				exchangeCandle.getOpenTime(),
				exchangeCandle.getCloseTime(),
				Double.parseDouble(exchangeCandle.getOpen()),
				Double.parseDouble(exchangeCandle.getHigh()),
				Double.parseDouble(exchangeCandle.getLow()),
				Double.parseDouble(exchangeCandle.getClose()),
				Double.parseDouble(exchangeCandle.getVolume())
		);
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
	public TimeInterval handlePollingException(String symbol, Exception e) {
		String message = "execute polling for " + symbol;
		if (e.getCause() instanceof TimeoutException) {
			log.error("Timeout trying to " + message, e);
		} else if (e.getCause() instanceof UnknownHostException) {
			log.error("Unable to " + message + ". Binance is offline.", e);
			return TimeInterval.minutes(1);
		} else {
			log.error("Error trying to " + message, e);
		}
		return null;
	}

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candlestick> consumer) {
		CandlestickInterval interval = CandlestickInterval.fromTimeInterval(tickInterval);
		log.info("Opening Binance {} live stream for: {}", tickInterval, symbols);
		socketClientCloseable = socketClient().onCandlestickEvent(symbols, interval, new BinanceApiCallback<>() {
			@Override
			public void onResponse(CandlestickEvent response) {
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

	@Override
	public Map<String, Double> getLatestPrices() {
		return restClient().getAllPrices().stream().collect(Collectors.toMap(TickerPrice::getSymbol, TickerPrice::getPriceAmount));
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		try {
			return Double.parseDouble(restClient().getPrice(assetSymbol + fundSymbol).getPrice());
		} catch (Exception e) {
			log.error("Error getting latest price of " + assetSymbol + fundSymbol, e);
			return -1.0;
		}
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
			BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(asyncHttpClient);
			socketClient = factory.newWebSocketClient();
		}
		return socketClient;
	}

	private BinanceApiRestClient restClient() {
		if (restClient == null) {
			BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(asyncHttpClient);
			restClient = factory.newRestClient();
		}
		return restClient;
	}

//	@Override
//	public boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol) {
//		return symbolInformation.containsKey(currentAssetSymbol + targetAssetSymbol);
//	}
}
