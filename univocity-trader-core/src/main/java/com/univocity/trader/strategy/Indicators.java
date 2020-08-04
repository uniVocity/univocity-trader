package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.indicators.base.adx.*;

import java.util.*;
import java.util.function.*;

public abstract class Indicators {

	public static HashMap<String, Indicator> instances = new HashMap<>();
	public static BiFunction<Indicator, Object[], String> keyBuilder = null;

	protected Indicators() {

	}

	protected synchronized static <T extends Indicator> T register(T indicator, Object... params) {
		if (keyBuilder != null) {
			String key = keyBuilder.apply(indicator, params);
			return (T) instances.computeIfAbsent(key, (k) -> indicator);
		}
		return indicator;
	}


	public static ADX ADX(TimeInterval interval) {
		return register(new ADX(interval), interval);
	}

	public static ADX ADX(int length, TimeInterval interval) {
		return register(new ADX(length, interval), length, interval);
	}

	public static ADX ADX(int diLength, int adxLength, TimeInterval interval) {
		return register(new ADX(diLength, adxLength, interval), diLength, adxLength, interval);
	}

	public static AggregatedTicksIndicator AggregatedTicksIndicator(TimeInterval interval) {
		return register(new AggregatedTicksIndicator(interval), interval);
	}

	public static AverageTrueRange AverageTrueRange(int length, TimeInterval interval) {
		return register(new AverageTrueRange(length, interval), length, interval);
	}

	public static AwesomeOscillator AwesomeOscillator(TimeInterval interval) {
		return register(new AwesomeOscillator(interval), interval);
	}

	public static AwesomeOscillator AwesomeOscillator(int lengthShort, int lengthLong, TimeInterval interval) {
		return register(new AwesomeOscillator(lengthShort, lengthLong, interval), lengthShort, lengthLong, interval);
	}

	public static BollingerBand BollingerBand(TimeInterval interval) {
		return register(new BollingerBand(interval), interval);
	}

	public static BollingerBand BollingerBand(int length, TimeInterval interval) {
		return register(new BollingerBand(length, interval), length, interval);
	}

	public static BollingerBand BollingerBand(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new BollingerBand(length, interval, valueGetter), length, interval, valueGetter);
	}


	public static BollingerBand BollingerBand(int length, double multiplier, TimeInterval interval) {
		return register(new BollingerBand(length, multiplier, interval), length, multiplier, interval);
	}

	public static BollingerBand BollingerBand(int length, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new BollingerBand(length, multiplier, interval, valueGetter), length, multiplier, interval, valueGetter);
	}

	public static ChandelierExitLong ChandelierExitLong(TimeInterval interval) {
		return register(new ChandelierExitLong(interval), interval);
	}

	public static ChandelierExitLong ChandelierExitLong(int length, TimeInterval interval, double k) {
		return register(new ChandelierExitLong(length, interval, k), length, interval, k);
	}

	public static ChandelierExitShort ChandelierExitShort(TimeInterval interval) {
		return register(new ChandelierExitShort(interval), interval);
	}

	public static ChandelierExitShort ChandelierExitShort(int length, TimeInterval interval, double k) {
		return register(new ChandelierExitShort(length, interval, k), length, interval, k);
	}

	public static ChangeIndicator ChangeIndicator(TimeInterval interval) {
		return register(new ChangeIndicator(interval), interval);
	}

	public static ChangeIndicator ChangeIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new ChangeIndicator(interval, valueGetter), interval, valueGetter);
	}

	public static CHOP CHOP(TimeInterval interval) {
		return register(new CHOP(interval), interval);
	}

	public static CHOP CHOP(int length, TimeInterval interval) {
		return register(new CHOP(length, interval), length, interval);
	}

	public static CHOP CHOP(int length, int scaleTo, TimeInterval interval) {
		return register(new CHOP(length, scaleTo, interval), length, scaleTo, interval);
	}

	public static ConnorsRSI ConnorsRSI(TimeInterval interval) {
		return register(new ConnorsRSI(interval), interval);
	}

	public static ConnorsRSI ConnorsRSI(int rsiLength, int streakRsiLength, int pctRankLength, TimeInterval interval) {
		return register(new ConnorsRSI(rsiLength, streakRsiLength, pctRankLength, interval), rsiLength, streakRsiLength, pctRankLength, interval);
	}

	public static <T extends SingleValueIndicator> DirectionIndicator<T> DirectionIndicator(T indicator) {
		return register(new DirectionIndicator<>(indicator), indicator);
	}

	public static <T extends SingleValueIndicator> DirectionIndicator<T> DirectionIndicator(T indicator, ToDoubleFunction<T> valueGetter) {
		return register(new DirectionIndicator<>(indicator, valueGetter), indicator, valueGetter);
	}

	public static DonchianChannel DonchianChannel(int length, TimeInterval interval) {
		return register(new DonchianChannel(length, interval), length, interval);
	}

	public static DoubleExponentialMovingAverage DoubleExponentialMovingAverage(int length, TimeInterval interval) {
		return register(new DoubleExponentialMovingAverage(length, interval), length, interval);
	}

	public static DoubleExponentialMovingAverage DoubleExponentialMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new DoubleExponentialMovingAverage(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static EldersForceIndex EldersForceIndex(TimeInterval interval) {
		return register(new EldersForceIndex(interval), interval);
	}

	public static EldersForceIndex EldersForceIndex(int length, TimeInterval interval) {
		return register(new EldersForceIndex(length, interval), length, interval);
	}

	public static EldersForceIndex EldersForceIndex(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new EldersForceIndex(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static ExponentialMovingAverage ExponentialMovingAverage(int length, TimeInterval interval) {
		return register(new ExponentialMovingAverage(length, interval), length, interval);
	}

	public static ExponentialMovingAverage ExponentialMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new ExponentialMovingAverage(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static HighestValueIndicator HighestValueIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new HighestValueIndicator(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static InstantaneousTrendline InstantaneousTrendline(TimeInterval interval) {
		return register(new InstantaneousTrendline(interval), interval);
	}

	public static InstantaneousTrendline InstantaneousTrendline(TimeInterval interval, boolean useHilbertTransform) {
		return register(new InstantaneousTrendline(interval, useHilbertTransform), interval, useHilbertTransform);
	}

	public static KAMA KAMA(TimeInterval interval) {
		return register(new KAMA(interval), interval);
	}

	public static KAMA KAMA(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new KAMA(interval, valueGetter), interval, valueGetter);
	}

	public static KAMA KAMA(int barCountEffectiveRatio, int barCountFast, int barCountSlow, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new KAMA(barCountEffectiveRatio, barCountFast, barCountSlow, interval, valueGetter), barCountEffectiveRatio, barCountFast, barCountSlow, interval, valueGetter);
	}

	public static KDJ KDJ(TimeInterval interval) {
		return register(new KDJ(interval), interval);
	}

	public static KDJ KDJ(int dLength, TimeInterval interval) {
		return register(new KDJ(dLength, interval), dLength, interval);
	}

	public static KDJ KDJ(int dLength, int kLength, TimeInterval interval) {
		return register(new KDJ(dLength, kLength, interval), dLength, kLength, interval);
	}

	public static KeltnerChannel KeltnerChannel(TimeInterval interval) {
		return register(new KeltnerChannel(interval), interval);
	}

	public static KeltnerChannel KeltnerChannel(int length, TimeInterval interval) {
		return register(new KeltnerChannel(length, interval), length, interval);
	}

	public static KeltnerChannel KeltnerChannel(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new KeltnerChannel(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static KeltnerChannel KeltnerChannel(int length, int atrLength, TimeInterval interval) {
		return register(new KeltnerChannel(length, atrLength, interval), length, atrLength, interval);
	}

	public static KeltnerChannel KeltnerChannel(int length, int atrLength, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new KeltnerChannel(length, atrLength, interval, valueGetter), length, atrLength, interval, valueGetter);
	}

	public static LowestValueIndicator LowestValueIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new LowestValueIndicator(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static MACD MACD(TimeInterval interval) {
		return register(new MACD(interval), interval);
	}

	public static MACD MACD(int shortCount, int longCount, int macdCount, TimeInterval interval) {
		return register(new MACD(shortCount, longCount, macdCount, interval), shortCount, longCount, macdCount, interval);
	}

	public static MACD MACD(int shortCount, int longCount, int macdCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new MACD(shortCount, longCount, macdCount, interval, valueGetter), shortCount, longCount, macdCount, interval, valueGetter);
	}

	public static MinusDIIndicator MinusDIIndicator(int length, TimeInterval interval) {
		return register(new MinusDIIndicator(length, interval), length, interval);
	}

	public static MinusDMIndicator MinusDMIndicator(TimeInterval interval) {
		return register(new MinusDMIndicator(interval), interval);
	}

	public static ModifiedMovingAverage ModifiedMovingAverage(int length, TimeInterval interval) {
		return register(new ModifiedMovingAverage(length, interval), length, interval);
	}

	public static ModifiedMovingAverage ModifiedMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new ModifiedMovingAverage(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static MovingAverage MovingAverage(int length, TimeInterval interval) {
		return register(new MovingAverage(length, interval), length, interval);
	}

	public static MovingAverage MovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new MovingAverage(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static RealBodyIndicator RealBodyIndicator(TimeInterval interval) {
		return register(new RealBodyIndicator(interval), interval);
	}

	public static MVWAP MVWAP(int length, int vwapLength, TimeInterval interval) {
		return register(new MVWAP(length, vwapLength, interval), length, vwapLength, interval);
	}

	public static OBV OBV(TimeInterval interval) {
		return register(new OBV(interval), interval);
	}

	public static ParabolicSAR ParabolicSAR(TimeInterval interval) {
		return register(new ParabolicSAR(interval), interval);
	}

	public static ParabolicSAR ParabolicSAR(double aF, double maxA, TimeInterval interval) {
		return register(new ParabolicSAR(aF, maxA, interval), aF, maxA, interval);
	}

	public static ParabolicSAR ParabolicSAR(double aF, double maxA, double increment, TimeInterval interval) {
		return register(new ParabolicSAR(aF, maxA, increment, interval), aF, maxA, increment, interval);
	}

	public static PercentRankIndicator PercentRankIndicator(TimeInterval interval) {
		return register(new PercentRankIndicator(interval), interval);
	}

	public static PercentRankIndicator PercentRankIndicator(int length, TimeInterval interval) {
		return register(new PercentRankIndicator(length, interval), length, interval);
	}

	public static PlusDIIndicator PlusDIIndicator(int length, TimeInterval interval) {
		return register(new PlusDIIndicator(length, interval), length, interval);
	}

	public static PlusDMIndicator PlusDMIndicator(TimeInterval interval) {
		return register(new PlusDMIndicator(interval), interval);
	}

	public static PVT PVT(TimeInterval interval) {
		return register(new PVT(interval), interval);
	}

	public static PVT PVT(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new PVT(interval, valueGetter), interval, valueGetter);
	}

	public static RateOfChange RateOfChange(int length, TimeInterval interval) {
		return register(new RateOfChange(length, interval), length, interval);
	}

	public static RSI RSI(TimeInterval interval) {
		return register(new RSI(interval), interval);
	}

	public static RSI RSI(int length, TimeInterval interval) {
		return register(new RSI(length, interval), length, interval);
	}


	public static StochasticOscillatorD StochasticOscillatorD(TimeInterval interval) {
		return register(new StochasticOscillatorD(interval), interval);
	}

	public static StochasticOscillatorD StochasticOscillatorD(int dLength, TimeInterval interval) {
		return register(new StochasticOscillatorD(dLength, interval), dLength, interval);
	}

	public static StochasticOscillatorD StochasticOscillatorD(int dLength, int kLength, TimeInterval interval) {
		return register(new StochasticOscillatorD(dLength, kLength, interval), dLength, kLength, interval);
	}

	public static StochasticOscillatorK StochasticOscillatorK(TimeInterval interval) {
		return register(new StochasticOscillatorK(interval), interval);
	}

	public static StochasticOscillatorK StochasticOscillatorK(int length, TimeInterval interval) {
		return register(new StochasticOscillatorK(length, interval), length, interval);
	}

	public static StochasticRSI StochasticRSI(TimeInterval interval) {
		return register(new StochasticRSI(interval), interval);
	}

	public static StochasticRSI StochasticRSI(int length, TimeInterval interval) {
		return register(new StochasticRSI(length, interval), length, interval);
	}

	public static StreakIndicator StreakIndicator(TimeInterval interval) {
		return register(new StreakIndicator(interval), interval);
	}

	public static StreakIndicator StreakIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new StreakIndicator(interval, valueGetter), interval, valueGetter);
	}

	public static TrueRange TrueRange(TimeInterval interval) {
		return register(new TrueRange(interval), interval);
	}

	public static TTMTrend TTMTrend(TimeInterval interval) {
		return register(new TTMTrend(interval), interval);
	}

	public static TTMTrend TTMTrend(int length, TimeInterval interval) {
		return register(new TTMTrend(length, interval), length, interval);
	}

	public static TTMTrend TTMTrend(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new TTMTrend(length, interval, valueGetter), length, interval, valueGetter);
	}

	public static VolumeRateOfChange VolumeRateOfChange(int length, TimeInterval interval) {
		return register(new VolumeRateOfChange(length, interval), length, interval);
	}

	public static VWAP VWAP(int length, TimeInterval interval) {
		return register(new VWAP(length, interval), length, interval);
	}

	public static WaddahAttarExplosion WaddahAttarExplosion(TimeInterval interval) {
		return register(new WaddahAttarExplosion(interval), interval);
	}

	public static WaddahAttarExplosion WaddahAttarExplosion(double sensitivity, int fastLength, int slowLength, int channelLength, double multiplier, TimeInterval interval) {
		return register(new WaddahAttarExplosion(sensitivity, fastLength, slowLength, channelLength, multiplier, interval), sensitivity, fastLength, slowLength, channelLength, multiplier, interval);
	}

	public static WaddahAttarExplosion WaddahAttarExplosion(double sensitivity, int fastLength, int slowLength, int channelLength, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new WaddahAttarExplosion(sensitivity, fastLength, slowLength, channelLength, multiplier, interval, valueGetter), sensitivity, fastLength, slowLength, channelLength, multiplier, interval, valueGetter);
	}

	public static WilliamsRIndicator WilliamsRIndicator(TimeInterval interval) {
		return register(new WilliamsRIndicator(interval), interval);
	}

	public static WilliamsRIndicator WilliamsRIndicator(int length, TimeInterval interval) {
		return register(new WilliamsRIndicator(length, interval), length, interval);
	}

	public static YoYoExitLong YoYoExitLong(TimeInterval interval) {
		return register(new YoYoExitLong(interval), interval);
	}

	public static YoYoExitLong YoYoExitLong(int length, TimeInterval interval, double k) {
		return register(new YoYoExitLong(length, interval, k), length, interval, k);
	}

	public static YoYoExitShort YoYoExitShort(TimeInterval interval) {
		return register(new YoYoExitShort(interval), interval);
	}

	public static YoYoExitShort YoYoExitShort(int length, TimeInterval interval, double k) {
		return register(new YoYoExitShort(length, interval, k), length, interval, k);
	}

	public static ZeroLagMovingAverage ZeroLagMovingAverage(int length, TimeInterval interval) {
		return register(new ZeroLagMovingAverage(length, interval), length, interval);
	}

	public static ZeroLagMovingAverage ZeroLagMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return register(new ZeroLagMovingAverage(length, interval, valueGetter), length, interval, valueGetter);
	}

}
