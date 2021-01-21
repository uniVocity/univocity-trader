package com.univocity.trader.examples;

import java.util.*;

import com.univocity.trader.account.*;
import org.slf4j.*;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author tom@khubla.com
 */
/**
 * attempt to buy when market below average and sell when its above average
 */
public class TrivialStrategy extends IndicatorStrategy {
	/**
	 * logger
	 */
	private static final Logger log = LoggerFactory.getLogger(TrivialStrategy.class);
	private static final double DEFAULT_BUY_GOAL = 0.10;
	private static final double DEFAULT_SELL_GOAL = 0.03;
	private double buygoal = DEFAULT_BUY_GOAL;
	private double sellgoal = DEFAULT_SELL_GOAL;
	/**
	 * indicators
	 */
	private final Set<Indicator> indicators = new HashSet<>();
	/**
	 * moving averages
	 */
	private final MovingAverage ma1day;

	public TrivialStrategy() {
		indicators.add(ma1day = new MovingAverage(10, TimeInterval.days(1)));
	}

	private double delta(double i1, double i2) {
		return (Math.abs((i1 - i2) / i1));
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	public double getBuygoal() {
		return buygoal;
	}

	public double getSellgoal() {
		return sellgoal;
	}

	@Override
	public Signal getSignal(Candle candle, Context context) {
		if (ma1day.getValue() > 0) {
			final double deltaH = delta(candle.high, ma1day.getValue());
			final double deltaL = delta(candle.low, ma1day.getValue());
			if (deltaH > buygoal) {
				if (candle.high < ma1day.getValue()) {
					// log.debug("market is below average by {}%", String.format("%.3f", delta * 100));
					/*
					 * market is below average by delta% and increasing
					 */
					return Signal.BUY;
				}
			}
			if (deltaL > sellgoal) {
				if (candle.low > ma1day.getValue()) {
					// log.debug("market is above average by {}%", String.format("%.3f", delta * 100));
					/*
					 * market is above average by delta% and decreasing
					 */
					return Signal.SELL;
				}
			}
		}
		return Signal.NEUTRAL;
	}

	public void setBuygoal(double buygoal) {
		this.buygoal = buygoal;
	}

	public void setSellgoal(double sellgoal) {
		this.sellgoal = sellgoal;
	}
}