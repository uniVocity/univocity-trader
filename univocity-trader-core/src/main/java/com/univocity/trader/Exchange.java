package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.time.*;
import java.util.*;

/**
 * An {@code Exchange} implementation provides information about available instruments and their prices in a live
 * trading broker or exchange.
 *
 * @param <T> the {@code Exchange}-specific candle/tick type, which will be converted to a {@link Candle} via
 *            {@link #generateCandle(Object)} for in-memory calculations and {@link PreciseCandle} with {@link #generatePreciseCandle(Object)}
 *            for storage.
 *
 * @see Candle
 * @see PreciseCandle
 * @see SymbolInformation
 * @see ClientAccount
 * @see com.univocity.trader.simulation.SimulatedExchange
 * @see com.univocity.trader.simulation.SimulatedClientAccount
 */
public interface Exchange<T> {

	/**
	 * Provides the latest exchange-specific candle/tick for a given symbol at the given time interval.
	 *
	 * Used for polling candle details from the exchange in case no updates are received from the live stream.
	 * The {@link LiveTrader} will continuously check for updates on the signals of the symbols subscribed to with
	 * {@link #openLiveStream(String, TimeInterval, TickConsumer)}.
	 *
	 * @param symbol   the symbol whose latest price information should be returned from the exchange (e.g. BTCUSDT, MSFT, etc)
	 * @param interval the duration of the candle. e.g. if {@code TimeInterval.minutes(5)} should ideally return the latest 5 minute candle
	 *                 (typically with open and close time, open and close prices, highest and lowest prices, and traded volume).
	 *                 Not every bit of information is required, but at least the closing price and the close time should be provided.
	 *
	 * @return the {@code Exchange}-specific candle/tick type, which will be converted to a {@link Candle} via
	 * {@link #generateCandle(Object)} for in-memory calculations and {@link PreciseCandle} with {@link #generatePreciseCandle(Object)}
	 * for storage.
	 */
	T getLatestTick(String symbol, TimeInterval interval);

	/**
	 * Provides the latest exchange-specific candles/ticks for a given symbol at the given time interval. This method is intended to return
	 * quickly so the list returned should not contain much more than the price information of the last 30 minutes.
	 *
	 * @param symbol   the symbol whose latest price information should be returned from the exchange (e.g. BTCUSDT, MSFT, etc)
	 * @param interval the duration of each candle. e.g. if {@code TimeInterval.minutes(5)} should ideally return the latest 5 minute candle
	 *                 (typically with open and close time, open and close prices, highest and lowest prices, and traded volume).
	 *                 Not every bit of information is required, but at least the closing price and the close time should be provided.
	 *
	 * @return a list with recent {@code Exchange}-specific candles/ticks, which will be converted to a {@link Candle} via
	 * {@link #generateCandle(Object)} for in-memory calculations and {@link PreciseCandle} with {@link #generatePreciseCandle(Object)}
	 * for storage.
	 */
	List<T> getLatestTicks(String symbol, TimeInterval interval);

	/**
	 * Returns historical candles/ticks for a given symbol at the given time interval, for a given time period. Used to populate
	 * the local database managed by {@link CandleRepository} for backtesting and {@link Strategy} development. If the live exchange
	 * limits the number of candles returned at once, that limit should be respected, and the interval given by
	 * {@code startTime} or {@code endTime} can be shortened internally.
	 *
	 * Method {@link CandleRepository#fillHistoryGaps(Exchange, String, Instant, TimeInterval)} will cycle through the gaps in history and fill them accordingly
	 * by requesting more candles from the exchange as needed.
	 *
	 * @param symbol    the symbol whose latest price information should be returned from the exchange (e.g. BTCUSDT, MSFT, etc)
	 * @param interval  the duration of each candle. e.g. if {@code TimeInterval.minutes(5)} should ideally return the latest 5 minute candle
	 *                  (typically with open and close time, open and close prices, highest and lowest prices, and traded volume).
	 *                  Not every bit of information is required, but at least the closing price and the close time should be provided.
	 * @param startTime the starting time (in milliseconds) of the historical data to be returned.
	 * @param endTime   the end time (in milliseconds) of the historical data to be returned.
	 *
	 * @return a list with historical {@code Exchange}-specific candles/ticks, for the given period (or less), which will be converted to a {@link Candle} via
	 * {@link #generateCandle(Object)} for in-memory calculations and {@link PreciseCandle} with {@link #generatePreciseCandle(Object)}
	 * for storage.
	 */
	List<T> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime);

	/**
	 * Converts an {@code Exchange}-specific candle/tick to a {@link Candle} that is used by the framework for fast in-memory calculations.
	 *
	 * At least the closing price and open/close times in the returned {@link Candle} should be populated.
	 *
	 * @param exchangeCandle the {@code Exchange}-specific candle/tick details whose data need to be converted into a {@link Candle}.
	 *
	 * @return a {@link Candle} populated with all details available from the given exchange-specific candle
	 */
	Candle generateCandle(T exchangeCandle);

	/**
	 * Converts an {@code Exchange}-specific candle/tick to a {@link PreciseCandle} that is used by the framework for database storage with
	 * no precision loss.
	 *
	 * At least the closing price and open/close times in the returned {@link PreciseCandle} should be populated. Set the fields of
	 * the {@link PreciseCandle} to {@code BigDecimal.ZERO} if the input candle does not have the corresponding data and it can't be derived.
	 *
	 * @param exchangeCandle the {@code Exchange}-specific candle/tick details whose data need to be converted into a {@link PreciseCandle}.
	 *
	 * @return a {@link PreciseCandle} populated with all details available from the given exchange-specific candle
	 */
	PreciseCandle generatePreciseCandle(T exchangeCandle);

	/**
	 * Connects to the live exchange stream to receive real time signals which will be delegated to a given {@link TickConsumer}.
	 *
	 * On top of the live stream, the {@link LiveTrader} will continuously check for updates on the signals of the symbols subscribed to with this method.
	 * If the {@link LiveTrader} does not receive price updates within the given {@code tickInterval}, symbol prices will be polled using
	 * {@link #getLatestTick(String, TimeInterval)}.
	 *
	 * @param symbols      a comma separated list of symbols to subscribe to.
	 * @param tickInterval the frequency of the signals to be received such as every 1 minute, 1 hour, 5 seconds, etc (whichever is supported by the exchange)
	 * @param consumer     a consumer of {@code Exchange}-specific candle/tick details whose data need to be converted into a {@link Candle} and then submitted
	 *                     for further processing (i.e. {@link Strategy} analysis, {@link Signal} generation and potential trading by {@link Client})
	 */
	void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<T> consumer);

	/**
	 * Disconnects from the live exchange stream opened with {@link #openLiveStream(String, TimeInterval, TickConsumer)}
	 *
	 * @throws Exception in case any error occurs closing the stream.
	 */
	void closeLiveStream() throws Exception;

	/**
	 * Quick price update on all available symbols. Used for calculating the current account position, available funds for trading
	 * and (in {@link AccountManager#allocateFunds(String)} and sending e-mail notifications with the current net worth of the account.
	 *
	 * @return a map of symbols traded by the exchange and their corresponding prices
	 */
	Map<String, Double> getLatestPrices();

	/**
	 * Returns the details about each traded symbol, such as minimum order size and decimal places to use for correct formatting and proper order
	 * generation (some exchanges may not accept prices such as {@code 14.44321} and would require {@code 14.4432} instead).
	 *
	 * If not applicable, simply return {@code null}
	 *
	 * @return a map with the {@link SymbolInformation} associated with all available symbols in this exchange, if applicable.
	 */
	Map<String, SymbolInformation> getSymbolInformation();

	/**
	 * Returns the latest price of a given symbol or trading pair
	 *
	 * @param assetSymbol symbol of the asset (e.g. BTC, EUR, MSFT, etc)
	 * @param fundSymbol  symbol of the funds used to represent the price of the given asset (ETH, USD, BRL). The framework will always send a symbol here, if
	 *                    no trading pair exists, the {@code referenceCurrency} provided by {@link Client#getReferenceCurrency()} will be sent.
	 *                    It can be ignored if not applicable (when trading stocks for example, and the account only buys stocks with USD)
	 *
	 * @return the latest price of the given {@code assetSymbol} or pair.
	 */
	double getLatestPrice(String assetSymbol, String fundSymbol);

	/**
	 * Opens a client account with the given {@code apiKey} and {@code secret}, which authorizes the {@link Client} to trade and update their account balance.
	 *
	 * @param apiKey the public API key provided by the exchange for a given account
	 * @param secret the secret key provided by the exchange for the {@link Client} account
	 *
	 * @return an exchange-specific implementation of the {@link ClientAccount} interface, which allows the {@link Client} to trade through this framework.
	 */
	ClientAccount connectToAccount(String apiKey, String secret);

	/**
	 * Handles errors from the exchange server that might be produced when polling for latest prices in case the live stream becomes unavailable, slow or
	 * unreliable.
	 *
	 * The {@link LiveTrader} will continuously check for updates on the signals of the symbols subscribed to with
	 * {@link #openLiveStream(String, TimeInterval, TickConsumer)}. If no updates are received within the given {@link TimeInterval}, the exchange will be
	 * polled using {@link #getLatestTick(String, TimeInterval)} to ensure the signals generated by the {@link Strategy}s in use are reliable.
	 *
	 * @param symbol the symbol whose latest candle/tick information did not come through the live stream, and was polled.
	 * @param e      the error that occurred when polling for the latest tick of the given symbol.
	 *
	 * @return a time to wait before retrying the execution of polling action.
	 */
	TimeInterval handlePollingException(String symbol, Exception e);

//	boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol);
}
