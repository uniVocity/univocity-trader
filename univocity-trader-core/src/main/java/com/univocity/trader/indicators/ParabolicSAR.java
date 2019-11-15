package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class ParabolicSAR extends SingleValueIndicator {

	private double accelerationFactor;
	private final double maxAcceleration;
	private final double accelerationIncrement;
	private final double accelerationStart;


	private boolean currentTrend; // true if uptrend, false otherwise
	private long startTrendIndex = 0; // index of start bar of the current trend
	private CircularList minPriceIndicator;
	private CircularList maxPriceIndicator;
	private double currentExtremePoint; // the extreme point of the current calculation
	private double minMaxExtremePoint; // depending on trend the maximum or minimum extreme point value of trend

	private double tmpSar = Double.MAX_VALUE;
	private double sar;

	private static final double[] instants = Indicator.populateInstants(5000);
	private CircularList sarValues = new CircularList(instants.length);
	private CircularList priceValues = new CircularList(instants.length);
	private int ticksOnTrend;


	/**
	 * Constructor with default parameters
	 */
	public ParabolicSAR(TimeInterval interval) {
		this(0.02, 0.2, 0.02, interval);

	}

	/**
	 * Constructor with custom parameters and default increment value
	 *
	 * @param aF   acceleration factor
	 * @param maxA maximum acceleration
	 */
	public ParabolicSAR(double aF, double maxA, TimeInterval interval) {
		this(aF, maxA, 0.02, interval);
	}

	/**
	 * Constructor with custom parameters
	 *
	 * @param aF        acceleration factor
	 * @param maxA      maximum acceleration
	 * @param increment the increment step
	 */
	public ParabolicSAR(double aF, double maxA, double increment, TimeInterval interval) {
		super(interval, null);
		maxPriceIndicator = new CircularList(5000);
		minPriceIndicator = new CircularList(5000);
		maxAcceleration = maxA;
		accelerationFactor = aF;
		accelerationIncrement = increment;
		accelerationStart = aF;
	}

	@Override
	public double getValue() {
		return tmpSar != Double.MAX_VALUE ? tmpSar : sar;
	}

	private void calculate(Candle candle, boolean updating) {
		double sar = this.sar;
		boolean currentTrend = this.currentTrend;
		double currentExtremePoint = this.currentExtremePoint;
		double minMaxExtremePoint = this.minMaxExtremePoint;

		if (getAccumulationCount() < 1) {
			this.tmpSar = Double.MAX_VALUE;
			this.sar = 0.0;
			return; // no trend detection possible for the first value
		} else if (getAccumulationCount() == 1) {// start trend detection
			currentTrend = maxPriceIndicator.getRecentValue(2) < candle.close;
			if (!currentTrend) { // down trend
				sar = candle.high; // put sar on max price of Candle
				currentExtremePoint = sar;
				minMaxExtremePoint = currentExtremePoint;
			} else { // up trend
				sar = candle.low; // put sar on min price of Candle
				currentExtremePoint = sar;
				minMaxExtremePoint = currentExtremePoint;

			}

			if (!updating) {
				this.tmpSar = Double.MAX_VALUE;
				this.sar = sar;
				this.currentTrend = currentTrend;
				this.currentExtremePoint = currentExtremePoint;
				this.minMaxExtremePoint = minMaxExtremePoint;
			} else {
				this.tmpSar = sar;
			}
			return;
		}

		long startTrendIndex = this.startTrendIndex;
		double accelerationFactor = this.accelerationFactor;
		int ticksOnTrend = this.ticksOnTrend;
		ticksOnTrend++;
		double priorSar = sar;

		if (currentTrend) { // if up trend
			sar = priorSar + (accelerationFactor * (currentExtremePoint - priorSar));
			currentTrend = candle.low > sar;
			if (!currentTrend) { // check if sar touches the min price
				ticksOnTrend = 1;
				sar = minMaxExtremePoint; // sar starts at the highest extreme point of previous up trend
				currentTrend = false; // switch to down trend and reset values
				startTrendIndex = getAccumulationCount();
				accelerationFactor = accelerationStart;
				currentExtremePoint = candle.low; // put point on max
				minMaxExtremePoint = currentExtremePoint;
			} else { // up trend is going on
				currentExtremePoint = maxPriceIndicator.getMax((int)(getAccumulationCount() - startTrendIndex));
				if (currentExtremePoint > minMaxExtremePoint) {
					accelerationFactor = incrementAcceleration(accelerationFactor);
					minMaxExtremePoint = currentExtremePoint;
				}

			}
		} else { // downtrend
			sar = priorSar - (accelerationFactor * (priorSar - currentExtremePoint));
			currentTrend = candle.high >= sar;
			if (currentTrend) { // check if switch to up trend
				ticksOnTrend = 1;
				sar = minMaxExtremePoint; // sar starts at the lowest extreme point of previous down trend
				accelerationFactor = accelerationStart;
				startTrendIndex = getAccumulationCount();
				currentExtremePoint = candle.high;
				minMaxExtremePoint = currentExtremePoint;
			} else { // down trend io going on
				currentExtremePoint = minPriceIndicator.getMin((int)(getAccumulationCount() - startTrendIndex));
				if (currentExtremePoint < minMaxExtremePoint) {
					accelerationFactor = incrementAcceleration(accelerationFactor);
					minMaxExtremePoint = currentExtremePoint;
				}
			}
		}

		if (!updating) {
			this.tmpSar = Double.MAX_VALUE;
			this.sar = sar;
			this.currentTrend = currentTrend;
			this.currentExtremePoint = currentExtremePoint;
			this.minMaxExtremePoint = minMaxExtremePoint;
			this.ticksOnTrend = ticksOnTrend;
			this.startTrendIndex = startTrendIndex;
			this.accelerationFactor = accelerationFactor;
			sarValues.add(sar);
			priceValues.add(candle.close);
		} else {
			tmpSar = sar;
		}
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			maxPriceIndicator.update(candle.high);
			minPriceIndicator.update(candle.low);
		} else {
			maxPriceIndicator.add(candle.high);
			minPriceIndicator.add(candle.low);
		}

		calculate(candle, updating);
		return true;
	}

	/**
	 * Increments the acceleration factor.
	 */
	private double incrementAcceleration(double accelerationFactor) {
		if (accelerationFactor >= maxAcceleration) {
			accelerationFactor = maxAcceleration;
		} else {
			accelerationFactor = accelerationFactor + accelerationIncrement;
		}
		return accelerationFactor;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
