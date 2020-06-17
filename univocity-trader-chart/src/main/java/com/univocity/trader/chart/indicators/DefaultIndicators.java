package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

import static com.univocity.trader.indicators.CHOP.*;

public class DefaultIndicators {

	public static ADX ADX(@PositiveDefault(14) int diLength, @PositiveDefault(14) int adxLength, TimeInterval interval) {
		return Indicators.ADX(diLength, adxLength, interval);
	}

	public static AggregatedTicksIndicator AggregatedTicksIndicator(TimeInterval interval) {
		return Indicators.AggregatedTicksIndicator(interval);
	}

	public static AverageTrueRange AverageTrueRange(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.AverageTrueRange(length, interval);
	}

	public static AwesomeOscillator AwesomeOscillator(@PositiveDefault(5) int lengthShort, @PositiveDefault(14) int lengthLong, TimeInterval interval) {
		return Indicators.AwesomeOscillator(lengthShort, lengthLong, interval);
	}

	@Overlay(label = "Bollinger")
	@Render(value = "getUpperBand", description = "High")
	@Render(value = "getMiddleBand", description = "Middle")
	@Render(value = "getLowerBand", description = "Low")
	public static BollingerBand BollingerBand(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.BollingerBand(length, interval);
	}

	@Overlay
	public static ChandelierExitLong ChandelierExitLong(@PositiveDefault(22) int length, TimeInterval interval, @Default(value = 3.0, increment = 0.01) double k) {
		return Indicators.ChandelierExitLong(length, interval, k);
	}

	@Overlay
	public static ChandelierExitShort ChandelierExitShort(@PositiveDefault(22) int length, TimeInterval interval, @Default(value = 3, increment = 0.01) double k) {
		return Indicators.ChandelierExitShort(length, interval, k);
	}

	public static ChangeIndicator ChangeIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.ChangeIndicator(interval, valueGetter(valueGetter));
	}

	@Underlay(min = 0.0, max = 100.0)
	@Render(value = "getValue")
	@Render(value = "getHighChoppinessValue", description = "High", constant = true)
	@Render(value = "getLowChoppinessValue", description = "Low", constant = true)
	public static CHOP CHOP(@PositiveDefault(14) int length, @PositiveDefault(value = HIGH_CHOPPINESS_VALUE, maximum = 100.0) double high, @PositiveDefault(value = LOW_CHOPPINESS_VALUE, maximum = 100) double low, TimeInterval interval) {
		CHOP out = Indicators.CHOP(length, 100, interval);
		out.setHighChoppinessValue(high);
		out.setLowChoppinessValue(low);
		return out;
	}

	public static ConnorsRSI ConnorsRSI(@PositiveDefault(3) int rsiLength, @PositiveDefault(2) int streakRsiLength, @PositiveDefault(100) int pctRankLength, TimeInterval interval) {
		return Indicators.ConnorsRSI(rsiLength, streakRsiLength, pctRankLength, interval);
	}

//	public static <T extends SingleValueIndicator> DirectionIndicator<T> DirectionIndicator(T indicator, ToDoubleFunction<T> valueGetter) {
//		return Indicators.DirectionIndicator(indicator, valueGetter);
//	}

	@Overlay
	public static DonchianChannel DonchianChannel(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.DonchianChannel(length, interval);
	}

	@Overlay
	public static DoubleExponentialMovingAverage DoubleExponentialMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.DoubleExponentialMovingAverage(length, interval, valueGetter(valueGetter));
	}

	public static EldersForceIndex EldersForceIndex(@PositiveDefault(13) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.EldersForceIndex(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static ExponentialMovingAverage ExponentialMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.ExponentialMovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static HighestValueIndicator HighestValueIndicator(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.HighestValueIndicator(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static InstantaneousTrendline InstantaneousTrendline(TimeInterval interval, boolean useHilbertTransform) {
		return Indicators.InstantaneousTrendline(interval, useHilbertTransform);
	}

	@Overlay
	public static KAMA KAMA(@PositiveDefault(10) int barCountEffectiveRatio, @PositiveDefault(2) int barCountFast, @PositiveDefault(30) int barCountSlow, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.KAMA(barCountEffectiveRatio, barCountFast, barCountSlow, interval, valueGetter(valueGetter));
	}

	public static KDJ KDJ(@PositiveDefault(3) int dLength, @PositiveDefault(3) int kLength, TimeInterval interval) {
		return Indicators.KDJ(dLength, kLength, interval);
	}

	@Overlay
	public static KeltnerChannel KeltnerChannel(@PositiveDefault(20) int length, @PositiveDefault(10) int atrLength, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.KeltnerChannel(length, atrLength, interval, valueGetter(valueGetter));
	}

	@Render(value = "getMacdSignal", description = "Signal")
	@Render(value = "getMacdLine", description = "Line")
	@Render(value = "getHistogram", renderer = HistogramRenderer.class)
	public static MACD MACD(@PositiveDefault(12) int shortCount, @PositiveDefault(26) int longCount, @PositiveDefault(9) int macdCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.MACD(shortCount, longCount, macdCount, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static LowestValueIndicator LowestValueIndicator(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.LowestValueIndicator(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static ModifiedMovingAverage ModifiedMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.ModifiedMovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static MovingAverage MovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.MovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static MVWAP MVWAP(@PositiveDefault(24) int length, @PositiveDefault(12) int vwapLength, TimeInterval interval) {
		return Indicators.MVWAP(length, vwapLength, interval);
	}

	public static OBV OBV(TimeInterval interval) {
		return Indicators.OBV(interval);
	}

	@Overlay
	public static ParabolicSAR ParabolicSAR(@PositiveDefault(value = 0.02, increment = 0.001) double aF, @PositiveDefault(value = 0.2, increment = 0.001) double maxA, @PositiveDefault(value = 0.02, increment = 0.001) double increment, TimeInterval interval) {
		return Indicators.ParabolicSAR(aF, maxA, increment, interval);
	}

	public static PVT PVT(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.PVT(interval, valueGetter(valueGetter));
	}

	public static RateOfChange RateOfChange(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.RateOfChange(length, interval);
	}

	public static RSI RSI(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.RSI(length, interval);
	}

	public static StochasticRSI StochasticRSI(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.StochasticRSI(length, interval);
	}

	public static TrueRange TrueRange(TimeInterval interval) {
		return Indicators.TrueRange(interval);
	}

	public static VolumeRateOfChange VolumeRateOfChange(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.VolumeRateOfChange(length, interval);
	}

	@Overlay
	public static VWAP VWAP(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.VWAP(length, interval);
	}

	@Overlay
	public static YoYoExitLong YoYoExitLong(@PositiveDefault(22) int length, TimeInterval interval, @Default(value = 3.0, increment = 0.01) double k) {
		return Indicators.YoYoExitLong(length, interval, k);
	}

	@Overlay
	public static YoYoExitShort YoYoExitShort(@PositiveDefault(22) int length, TimeInterval interval, @Default(value = 3.0, increment = 0.01) double k) {
		return Indicators.YoYoExitShort(length, interval, k);
	}

	@Overlay
	public static ZeroLagMovingAverage ZeroLagMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.ZeroLagMovingAverage(length, interval, valueGetter(valueGetter));
	}

	private static ToDoubleFunction<Candle> valueGetter(ToDoubleFunction<Candle> valueGetter) {
		return valueGetter == null ? c -> c.close : valueGetter;
	}

}
