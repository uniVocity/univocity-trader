package com.univocity.trader.strategy.simple;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.trader.TradingFees;
import com.univocity.trader.account.Order;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.strategy.StrategyMonitor;

public class TradingfeeStrategyMonitor extends StrategyMonitor {
   private static final double MAX_FEES = 0.10;
   private static final Logger log = LoggerFactory.getLogger(TradingfeeStrategyMonitor.class);

   public TradingfeeStrategyMonitor() {
   }

   @Override
   protected Set<Indicator> getAllIndicators() {
      return null;
   }

   @Override
   public boolean discardSell(Candle candle) {
      TradingFees tradingFees = trader.getTradingFees();
      double quantity = this.trader.getAssetQuantity();
      double sellValue = tradingFees.takeFee(candle.low * quantity, Order.Type.MARKET, Order.Side.SELL);
      double feepct = ((candle.low * quantity) - sellValue) / sellValue;
      if (feepct > MAX_FEES) {
         /*
          * reject sales where we pay more than MAX_FEES in fees
          */
         log.debug("trading fees of  of {}% rejected", String.format("%.3f", feepct * 100));
         return true;
      }
      return false;
   }
}
