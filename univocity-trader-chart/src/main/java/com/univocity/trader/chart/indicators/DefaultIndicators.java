package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.chart.charts.theme.indicator.*;
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

	@Underlay
	@Render(value = "getValue", renderer = HistogramStreakRenderer.class, theme = HistogramTheme.class)
	public static AwesomeOscillator AwesomeOscillator(@PositiveDefault(5) int lengthShort, @PositiveDefault(14) int lengthLong, TimeInterval interval) {
		return Indicators.AwesomeOscillator(lengthShort, lengthLong, interval);
	}

	@Overlay(label = "Bollinger")
	@Render(value = "getUpperBand", description = "High", theme = UpperBand.class)
	@Render(value = "getMiddleBand", description = "Middle", theme = MiddleBand.class)
	@Render(value = "getLowerBand", description = "Low", theme = LowerBand.class)
	public static BollingerBand BollingerBand(@PositiveDefault(12) int length, @Default(value = 2.0, increment = 0.01) double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.BollingerBand(length, interval, valueGetter);
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
	@Render(value = "getValue", theme = BlueLine.class)
	@Render(value = "getHighChoppinessValue", description = "High", displayValue = false, theme = BoundaryLineTheme.class)
	@Render(value = "getLowChoppinessValue", description = "Low", displayValue = false, theme = BoundaryLineTheme.class)
	public static CHOP CHOP(@PositiveDefault(14) int length, @PositiveDefault(value = HIGH_CHOPPINESS_VALUE, maximum = 100.0) double high, @PositiveDefault(value = LOW_CHOPPINESS_VALUE, maximum = 100) double low, TimeInterval interval) {
		CHOP out = Indicators.CHOP(length, 100, interval);
		out.setHighChoppinessValue(high);
		out.setLowChoppinessValue(low);
		return out;
	}

	@Underlay(min = 0.0, max = 100.0)
	@Render(value = "getValue", theme = BlueLine.class)
	@Render(value = "getUpperBound", description = "High", displayValue = false, theme = BoundaryLineTheme.class)
	@Render(value = "getLowerBound", description = "Low", displayValue = false, theme = BoundaryLineTheme.class)
	public static ConnorsRSI ConnorsRSI(@PositiveDefault(3) int rsiLength, @PositiveDefault(2) int streakRsiLength, @PositiveDefault(100) int pctRankLength, @PositiveDefault(value = ConnorsRSI.UPPER_BOUND, maximum = 100.0) double high, @PositiveDefault(value = ConnorsRSI.LOWER_BOUND, maximum = 100) double low, TimeInterval interval) {
		ConnorsRSI out = Indicators.ConnorsRSI(rsiLength, streakRsiLength, pctRankLength, interval);
		out.setUpperBound(high);
		out.setLowerBound(low);
		return out;
	}

	public static CoppockCurve CoppockCurve(@PositiveDefault(14) int longRoCLength, @PositiveDefault(11) int shortRoCLength, @PositiveDefault(10) int wmaLength, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.CoppockCurve(longRoCLength, shortRoCLength, wmaLength, interval, valueGetter);
	}

	public static DetrendedPriceOscillator DetrendedPriceOscillator(@PositiveDefault(9) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.DetrendedPriceOscillator(length, interval, valueGetter);
	}

//	public static <T extends SingleValueIndicator> DirectionIndicator<T> DirectionIndicator(T indicator, ToDoubleFunction<T> valueGetter) {
//		return Indicators.DirectionIndicator(indicator, valueGetter);
//	}

	@Overlay
	@Render(value = "getUpperBand", description = "High", theme = UpperBand.class)
	@Render(value = "getMiddleBand", description = "Middle", theme = MiddleBand.class)
	@Render(value = "getLowerBand", description = "Low", theme = LowerBand.class)
	public static DonchianChannel DonchianChannel(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.DonchianChannel(length, interval);
	}

	@Overlay
	public static DoubleExponentialMovingAverage DoubleExponentialMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.DoubleExponentialMovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Render(value = "getValue", theme = BlueLine.class)
	@Render(constant = 0.0, theme = BoundaryLineTheme.class)
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
	public static HullMovingAverage HullMovingAverage(@PositiveDefault(9) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.HullMovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static IchimokuChikouSpan IchimokuChikouSpan(@PositiveDefault(26) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.IchimokuChikouSpan(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	@Compose(
			renderer = AreaRenderer.class,
			elements = {
					@Render(value = "getZl", description = "ZL"),
					@Render(value = "getTrendLine", description = "TL")
			}
	)
	public static InstantaneousTrendline InstantaneousTrendline(TimeInterval interval, boolean useHilbertTransform) {
		return Indicators.InstantaneousTrendline(interval, useHilbertTransform);
	}

	@Overlay
	public static KAMA KAMA(@PositiveDefault(10) int barCountEffectiveRatio, @PositiveDefault(2) int barCountFast, @PositiveDefault(30) int barCountSlow, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.KAMA(barCountEffectiveRatio, barCountFast, barCountSlow, interval, valueGetter(valueGetter));
	}

	@Render(value = "k", theme = BlueLine.class)
	@Render(value = "d", theme = RedLine.class)
	@Render(value = "j", theme = GreenLine.class)
	public static KDJ KDJ(@PositiveDefault(3) int dLength, @PositiveDefault(3) int kLength, TimeInterval interval) {
		return Indicators.KDJ(dLength, kLength, interval);
	}

	@Overlay
	@Render(value = "getUpperBand", description = "High", theme = UpperBand.class)
	@Render(value = "getMiddleBand", description = "Middle", theme = MiddleBand.class)
	@Render(value = "getLowerBand", description = "Low", theme = LowerBand.class)
	public static KeltnerChannel KeltnerChannel(@PositiveDefault(20) int length, @PositiveDefault(10) int atrLength, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.KeltnerChannel(length, atrLength, interval, valueGetter(valueGetter));
	}

	@Render(value = "getMacdSignal", description = "Signal", theme = RedLine.class)
	@Render(value = "getMacdLine", description = "Line", theme = BlueLine.class)
	@Render(value = "getHistogram", renderer = HistogramRenderer.class)
	public static MACD MACD(@PositiveDefault(12) int shortCount, @PositiveDefault(26) int longCount, @PositiveDefault(9) int macdCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.MACD(shortCount, longCount, macdCount, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static LinearlyWeightedMovingAverage LinearlyWeightedMovingAverage(@PositiveDefault(14) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.LinearlyWeightedMovingAverage(length, interval, valueGetter);
	}

	@Overlay
	public static LowestValueIndicator LowestValueIndicator(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.LowestValueIndicator(length, interval, valueGetter(valueGetter));
	}

	public static MassIndex MassIndex(@PositiveDefault(9) int emaBarCount, @PositiveDefault(25) int length, TimeInterval interval) {
		return Indicators.MassIndex(emaBarCount, length, interval);
	}

	@Overlay
	public static ModifiedMovingAverage ModifiedMovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.ModifiedMovingAverage(length, interval, valueGetter(valueGetter));
	}

	@Overlay
	public static MovingAverage MovingAverage(@PositiveDefault(12) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.MovingAverage(length, interval, valueGetter(valueGetter));
	}

	public static RealBodyIndicator RealBodyIndicator(TimeInterval interval) {
		return Indicators.RealBodyIndicator(interval);
	}

	public static RangeActionVerificationIndex RangeActionVerificationIndex(int shortSmaBarCount, int longSmaBarCount, TimeInterval interval) {
		return Indicators.RangeActionVerificationIndex(shortSmaBarCount, longSmaBarCount, interval);
	}

	@Overlay
	public static MVWAP MVWAP(@PositiveDefault(24) int length, @PositiveDefault(12) int vwapLength, TimeInterval interval) {
		return Indicators.MVWAP(length, vwapLength, interval);
	}

	public static OBV OBV(TimeInterval interval) {
		return Indicators.OBV(interval);
	}

	@Overlay
	@Render(renderer = MarkerRenderer.class)
	public static ParabolicSAR ParabolicSAR(@PositiveDefault(value = 0.02, increment = 0.001) double aF, @PositiveDefault(value = 0.2, increment = 0.001) double maxA, @PositiveDefault(value = 0.02, increment = 0.001) double increment, TimeInterval interval) {
		return Indicators.ParabolicSAR(aF, maxA, increment, interval);
	}

	@Render(value = "getSignal", description = "Signal", theme = RedLine.class)
	@Render(value = "getValue", description = "PPO", theme = BlueLine.class)
	@Render(value = "getHistogram", renderer = HistogramRenderer.class)
	public static PercentagePriceOscillator PercentagePriceOscillator(@PositiveDefault(12) int shortBarCount, @PositiveDefault(26) int longBarCount, @PositiveDefault(9) int signalBarCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.PercentagePriceOscillator(shortBarCount, longBarCount, signalBarCount, interval, valueGetter);
	}

	public static PVT PVT(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.PVT(interval, valueGetter(valueGetter));
	}

	@Render(value = "getRwiHigh", description = "High", theme = RedLine.class)
	@Render(value = "getRwiLow", description = "Low", theme = BlueLine.class)
	public static RandomWalkIndex RandomWalkIndex(@PositiveDefault(8) int length, TimeInterval interval) {
		return Indicators.RandomWalkIndex(length, interval);
	}

	@Render(value = "getOscillator", description = "RWI Oscillator", theme = BlueLine.class)
	@Render(constant = 0.0)
	public static RandomWalkIndex RandomWalkIndexOscillator(@PositiveDefault(8) int length, TimeInterval interval) {
		return Indicators.RandomWalkIndex(length, interval);
	}

	@Render(constant = 0.0)
	@Render(value = "getValue", theme = BlueLine.class)
	public static RateOfChange RateOfChange(@PositiveDefault(12) int length, TimeInterval interval) {
		return Indicators.RateOfChange(length, interval);
	}

	@Underlay(min = 0.0, max = 100.0)
	@Render(value = "getValue", theme = BlueLine.class)
	@Render(value = "getUpperBound", description = "High", displayValue = false, theme = BoundaryLineTheme.class)
	@Render(value = "getLowerBound", description = "Low", displayValue = false, theme = BoundaryLineTheme.class)
	public static RSI RSI(@PositiveDefault(14) int length, @PositiveDefault(value = RSI.UPPER_BOUND, maximum = 100.0) double high, @PositiveDefault(value = RSI.LOWER_BOUND, maximum = 100) double low, TimeInterval interval) {
		RSI out = Indicators.RSI(length, interval);
		out.setUpperBound(high);
		out.setLowerBound(low);
		return out;
	}

	@Underlay(min = 0.0, max = 100.0)
	@Render(value = "getValue", theme = BlueLine.class)
	@Render(value = "getUpperBound", description = "High", displayValue = false, theme = BoundaryLineTheme.class)
	@Render(value = "getLowerBound", description = "Low", displayValue = false, theme = BoundaryLineTheme.class)
	public static StochasticRSI StochasticRSI(@PositiveDefault(14) int length, @PositiveDefault(value = StochasticRSI.UPPER_BOUND, maximum = 100.0) double high, @PositiveDefault(value = StochasticRSI.LOWER_BOUND, maximum = 100) double low, TimeInterval interval) {
		StochasticRSI out = Indicators.StochasticRSI(length, interval);
		out.setLowerBound(low);
		out.setUpperBound(high);
		return out;
	}

	@Underlay(min = 0.0, max = 100.0)
	@Render(value = "k", theme = BlueLine.class)
	@Render(value = "d", theme = RedLine.class)
	@Render(value = "getUpperBound", description = "High", displayValue = false, theme = BoundaryLineTheme.class)
	@Render(value = "getLowerBound", description = "Low", displayValue = false, theme = BoundaryLineTheme.class)
	public static StochasticOscillatorD StochasticOscillator(@PositiveDefault(3) int dLength, @PositiveDefault(14) int kLength, @PositiveDefault(value = StochasticOscillatorD.UPPER_BOUND, maximum = 100.0) double high, @PositiveDefault(value = StochasticOscillatorD.LOWER_BOUND, maximum = 100) double low, TimeInterval interval) {
		StochasticOscillatorD out = Indicators.StochasticOscillatorD(dLength, kLength, interval);
		out.setLowerBound(low);
		out.setUpperBound(high);
		return out;
	}

	public static ThreeBlackCrows ThreeBlackCrows(@PositiveDefault(3) int length, @PositiveDefault(value = 0.3, increment = 0.01) double factor, TimeInterval interval) {
		return Indicators.ThreeBlackCrows(length, factor, interval);
	}

	public static ThreeWhiteSoldiers ThreeWhiteSoldiers(@PositiveDefault(3) int length, @PositiveDefault(value = 0.3, increment = 0.01) double factor, TimeInterval interval) {
		return Indicators.ThreeWhiteSoldiers(length, factor, interval);
	}

	public static TrueRange TrueRange(TimeInterval interval) {
		return Indicators.TrueRange(interval);
	}

	public static UlcerIndex UlcerIndex(@PositiveDefault(14) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.UlcerIndex(length, interval, valueGetter);
	}

	@Render(renderer = ColoredHistogramRenderer.class, args = "isMoveToUpside")
	public static Volume Volume(TimeInterval interval) {
		return Indicators.Volume(interval);
	}

	public static VolumeRateOfChange VolumeRateOfChange(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.VolumeRateOfChange(length, interval);
	}

	@Overlay
	public static VWAP VWAP(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.VWAP(length, interval);
	}


	@Render(value = "getExplosion", theme = GreenLine.class)
	@Render(value = "getTrend", renderer = ColoredHistogramRenderer.class, args = "isTrendUp")
	public static WaddahAttarExplosion WaddahAttarExplosion(@PositiveDefault(150) double sensitivity, @PositiveDefault(20) int fastLength, @PositiveDefault(40) int slowLength, @PositiveDefault(20) int channelLength, @PositiveDefault(value = 2.0, increment = 0.01) double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.WaddahAttarExplosion(sensitivity, fastLength, slowLength, channelLength, multiplier, interval, valueGetter);
	}

	@Overlay
	public static WeightedMovingAverage WeightedMovingAverage(@PositiveDefault(3) int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return Indicators.WeightedMovingAverage(length, interval, valueGetter(valueGetter));
	}

	public static WilliamsR WilliamsR(@PositiveDefault(14) int length, TimeInterval interval) {
		return Indicators.WilliamsR(length, interval);
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
