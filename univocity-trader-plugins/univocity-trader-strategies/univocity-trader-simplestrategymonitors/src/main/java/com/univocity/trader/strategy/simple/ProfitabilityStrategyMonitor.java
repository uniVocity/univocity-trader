package com.univocity.trader.strategy.simple;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.trader.TradingFees;
import com.univocity.trader.account.Order;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.strategy.StrategyMonitor;

public class ProfitabilityStrategyMonitor extends StrategyMonitor {
   /*
    * 1% ensures that we don't make losing sales, and lets the strategy class determine to sell or not
    */
   private static final double MINIMUM_PROFIT = 0.01;
   private static final Logger log = LoggerFactory.getLogger(ProfitabilityStrategyMonitor.class);

   public ProfitabilityStrategyMonitor() {
   }

   @Override
   protected Set<Indicator> getAllIndicators() {
      return null;
   }

   @Override
   public boolean discardSell(Candle candle) {
      TradingFees tradingFees = trader.getTradingFees();
      double quantity = this.trader.getAssetQuantity();
      double price = this.trader.getBoughtPrice();
      double purchaseValue = price * quantity;
      double sellValue = tradingFees.takeFee(candle.low * quantity, Order.Type.MARKET, Order.Side.SELL);
      double diffpct = (sellValue - purchaseValue) / purchaseValue;
      if (diffpct < MINIMUM_PROFIT) {
         /*
          * reject sales where we make less than MINIMUM_PROFIT profit
          */
         log.debug("trade profitability of {}% rejected", String.format("%.3f", diffpct * 100));
         return true;
      }
      return false;
   }
}
