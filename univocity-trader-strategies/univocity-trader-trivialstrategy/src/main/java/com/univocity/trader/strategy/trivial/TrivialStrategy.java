package com.univocity.trader.strategy.trivial;

import java.util.HashSet;
import java.util.Set;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.MovingAverage;
import com.univocity.trader.indicators.Signal;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.strategy.IndicatorStrategy;

/**
 * @author tom@khubla.com
 */
public class TrivialStrategy extends IndicatorStrategy {
   private static final double GOAL = 0.05;
   private final Set<Indicator> indicators = new HashSet<>();
   private final MovingAverage ma;

   public TrivialStrategy() {
      indicators.add(ma = new MovingAverage(20, TimeInterval.minutes(5)));
   }

   @Override
   protected Set<Indicator> getAllIndicators() {
      return indicators;
   }

   @Override
   public Signal getSignal(Candle candle) {
      if (ma.getValue() > 0) {
         double delta = delta(candle.high, ma.getValue());
         if (delta > GOAL) {
            if (candle.high < ma.getValue()) {
               /*
                * market is below average by delta%
                */
               return Signal.BUY;
            }
            if (candle.low > ma.getValue()) {
               /*
                * market is above average by delta%
                */
            }
            return Signal.SELL;
         }
      }
      return Signal.NEUTRAL;
   }

   private double delta(double i1, double i2) {
      return (Math.abs((i1 - i2) / i1));
   }
}
