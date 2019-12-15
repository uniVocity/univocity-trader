package com.univocity.trader.strategy.trivial;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.MovingAverage;
import com.univocity.trader.indicators.Signal;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.strategy.IndicatorStrategy;
import com.univocity.trader.strategy.Tuneable;

/**
 * @author tom@khubla.com
 */
public class TrivialStrategy extends IndicatorStrategy implements Tuneable {
   private static final Logger log = LoggerFactory.getLogger(TrivialStrategy.class);
   private static final double DEFAULT_BUY_GOAL = 0.05;
   private static final double DEFAULT_SELL_GOAL = 0.05;
   private double buygoal = DEFAULT_BUY_GOAL;
   private double sellgoal = DEFAULT_SELL_GOAL;
   private final Set<Indicator> indicators = new HashSet<>();
   private final MovingAverage ma;

   public TrivialStrategy() {
      indicators.add(ma = new MovingAverage(20, TimeInterval.minutes(5)));
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
   public Signal getSignal(Candle candle) {
      if (ma.getValue() > 0) {
         final double delta = delta(candle.high, ma.getValue());
         if (delta > buygoal) {
            if (candle.high < ma.getValue()) {
               log.debug("market is below average by {}%", String.format("%.3f", delta * 100));
               /*
                * market is below average by delta%
                */
               return Signal.BUY;
            }
         }
         if (delta > sellgoal) {
            if (candle.low > ma.getValue()) {
               log.debug("market is above average by {}%", String.format("%.3f", delta * 100));
               /*
                * market is above average by delta%
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

   @Override
   public void setParameter(int index, double value) {
      switch (index) {
         case 0:
            buygoal = value;
            break;
         case 1:
            sellgoal = value;
            break;
         default:
            break;
      }
   }

   public void setSellgoal(double sellgoal) {
      this.sellgoal = sellgoal;
   }
}
