package com.univocity.trader.account;

import static com.univocity.trader.indicators.Signal.BUY;
import static com.univocity.trader.indicators.Signal.NEUTRAL;
import static com.univocity.trader.indicators.Signal.SELL;
import static com.univocity.trader.utils.NewInstances.getInstances;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.TradingFees;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.indicators.Signal;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.simulation.Parameters;
import com.univocity.trader.strategy.Engine;
import com.univocity.trader.strategy.Strategy;
import com.univocity.trader.strategy.StrategyMonitor;
import com.univocity.trader.utils.InstancesProvider;

/**
 * A {@code Trader} is responsible for the lifecycle of a trade and provides all information associated with an open position. It is made available to the user via a {@link StrategyMonitor} to provide
 * information about trades opened by a {@link Strategy}. Once a {@link Strategy} returns a {@link Signal}, the {@link Engine} responsible for the symbol being traded will call
 * {@link Trader#trade(Candle, Signal, Strategy)}, who will then decide whether to buy, sell or ignore the signal, mostly based on its {@link #getStrategyMonitors()} decision.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see StrategyMonitor
 * @see Strategy
 */
public class Trader {
   private static final Logger log = LoggerFactory.getLogger(Trader.class);
   private Map<Double, Double> boughtPrices = new HashMap<>();
   private double boughtPrice;
   private double change;
   private long tradeStartTime;
   private long tradeDuration;
   private int ticks;
   private boolean stopped;
   private double max = Double.MIN_VALUE;
   private double min = Double.MAX_VALUE;
   private double maxChange;
   private double minChange;
   private String exitReason;
   final TradingManager tradingManager;
   private final StrategyMonitor[] monitors;
   private Candle latestCandle;
   private Strategy boughtStrategy;
   private Parameters parameters;
   private final boolean allowMixedStrategies;

   /**
    * Creates a new trader for a given symbol. For internal use only.
    *
    * @param tradingManager the object responsible for managing the entire trading workflow of a symbol
    * @param monitorProvider the provider of {@link StrategyMonitor} instances
    * @param params optional parameter set used for parameter optimization which is passed on to the {@link StrategyMonitor} instances created by the given monitorProvider
    * @param allInstances all known instances of {@link StrategyMonitor} that have been created so far, used to validate no single {@link StrategyMonitor} instance is shared among different
    *        {@code Trader} instances.
    */
   public Trader(TradingManager tradingManager, InstancesProvider<StrategyMonitor> monitorProvider, Parameters params, Set<Object> allInstances) {
      this.parameters = params;
      this.tradingManager = tradingManager;
      this.tradingManager.trader = this;
      this.monitors = monitorProvider == null ? new StrategyMonitor[0] : getInstances(tradingManager.getSymbol(), parameters, monitorProvider, "StrategyMonitor", false, allInstances);
      boolean allowMixedStrategies = true;
      for (int i = 0; i < this.monitors.length; i++) {
         this.monitors[i].setTrader(this);
         allowMixedStrategies &= this.monitors[i].allowMixedStrategies();
      }
      this.allowMixedStrategies = allowMixedStrategies;
   }

   /**
    * Returns the parameters used by the {@link StrategyMonitor} instances in this {@code Trader} instance. Used mainly to report which parameters are being used in a parameter optimization process.
    *
    * @return the parameters tested in the {@link StrategyMonitor} instances of this {@code Trader}.
    */
   public Parameters getParameters() {
      return parameters;
   }

   /**
    * Returns all {@link StrategyMonitor} instances built in the constructor of this class, which will be used by an {@link Engine} that processes candles for the symbol traded by this {@code Trader}
    *
    * @return all strategy monitors used by this {@code Trader}.
    */
   public StrategyMonitor[] getStrategyMonitors() {
      return monitors;
   }

   /**
    * Takes a trading decision based on the signal generated from a strategy. Once an {@link Order} is placed in the {@link Exchange}, this {@code Trader} object will start capturing the following
    * information:
    * <ul>
    * <li>the number of ticks received since the trade opened via {@link #getTicks()}</li>
    * <li>the price paid for the instrument traded by this {@code Trader} via {@link #getBoughtPrice()}</li>
    * <li>the time elapsed since the the instrument was bought via {@link #getTradeDuration()};</li>
    * <li>the current change % in price since this trade opened via {@link #getChange()}</li>
    * <li>the maximum positive change % this trade had via {@link #getMaxChange()}</li>
    * <li>the maximum price reached since the trade opened via {@link #getMaxPrice()}</li>
    * <li>the minimum change % (negative or zero) this trade had via {@link #getMinChange()}</li>
    * <li>the minimum price reached since the trade opened via {@link #getMinPrice()}</li>
    * <li>the latest closing price of the instrument traded via {@link #getLastClosingPrice()}</li>
    * <li>the minimum positive change % required to break even after fees, via {@link #getBreakEvenChange()}</li>
    * </ul>
    * The actions taken by the trader depend on the signal received:
    * <ul>
    * <li>signal = {@code BUY}: If no trades are open for the given symbol (i.e. when {@link TradingManager#hasAssets(Candle)} evaluates to {@code false}), a {@code BUY} order might be submitted when:
    * <ol>
    * <li>there are funds available to purchase that asset (via {@link TradingManager#allocateFunds()});</li>
    * <li>none of the associated strategy monitors (from {@link #getStrategyMonitors()} produce {@link StrategyMonitor#discardBuy(Strategy)};</li>
    * <li>the {@link OrderRequest} processed by the {@link OrderManager} associated with the symbol is not cancelled (i.e. {@link OrderRequest#isCancelled()})</li>
    * </ol>
    * </li>
    * <li>signal = {@code SELL}: Sells all assets held for the current symbol, closing any open orders, if:
    * <ol>
    * <li>the account has assets available to sell (via {@link TradingManager#hasAssets(Candle)});</li>
    * <li>none of the associated strategy monitors (from {@link #getStrategyMonitors()} produce {@code false} upon invoking {@link StrategyMonitor#allowExit()};</li>
    * <li>the {@link OrderRequest} processed by the {@link OrderManager} associated with the symbol is not cancelled (i.e. {@link OrderRequest#isCancelled()})</li>
    * </ol>
    * After the {@link Order} is placed in the {@link Exchange} this {@code Trader} will hold the previous trade information until a new trade is opened. If one of the strategy monitors returns
    * {@code false} for {@link StrategyMonitor#allowMixedStrategies()}, then only {@code SELL} signals that come from the same {@link Strategy} that generated the original {@code BUY} signal will be
    * accepted.</li>
    * <li>signal = {@code NEUTRAL}: Will simply update the statistics of any open trades.</li>
    * </ul>
    * When there is a trade open (i.e. {@link TradingManager#hasAssets(Candle)} evaluates to {@code true}), regardless of the signal received, all strategy monitors (from
    * {@link #getStrategyMonitors()} will have their {@link StrategyMonitor#handleStop(Signal, Strategy)} method called to determine whether or not to exit the trade. If any one of these calls return
    * an exit message, the assets will be sold, {@link #stopped()} will evaluate to {@code true} and {@link #exitReason()} will return the reason for exiting the trade.
    *
    * @param candle the latest candle received for the symbol traded by this {@code Trader}
    * @param signal the signal generated by the given strategy after receiving the given candle
    * @param strategy the strategy that originated the signal
    * @return a signal indicating the action taken by this {@code Trader}, i.e. {@code BUY} if it bought assets, {@code SELL} if assets were sold for whatever reason, and {@code NEUTRAL} if no action
    *         taken.
    */
   public Signal trade(Candle candle, Signal signal, Strategy strategy) {
      latestCandle = candle;
      exitReason = signal == SELL ? "sell signal" : null;
      if (signal == SELL && !allowMixedStrategies && this.boughtStrategy != null && strategy != this.boughtStrategy) {
         log.debug("Cleared sell signal of {}: trade opened by another strategy", getSymbol());
         signal = NEUTRAL;
      }
      stopped = false;
      boolean hasAssets = tradingManager.hasAssets(candle);
      if (hasAssets) {
         double nextChange = priceChangePct(candle.close);
         ticks++;
         if (max < candle.close) {
            max = candle.close;
         }
         if (min > candle.close) {
            min = candle.close;
         }
         change = nextChange;
         double prevMax = maxChange;
         maxChange = priceChangePct(max);
         if (maxChange > prevMax) {
            for (int i = 0; i < monitors.length; i++) {
               monitors[i].highestProfit(maxChange);
            }
         }
         double prevMin = minChange;
         minChange = priceChangePct(min);
         if (minChange < prevMin) {
            for (int i = 0; i < monitors.length; i++) {
               monitors[i].worstLoss(minChange);
            }
         }
         for (int i = 0; i < monitors.length; i++) {
            String exit = monitors[i].handleStop(signal, strategy);
            if (exit != null) {
               exitReason = exit;
               stopped = true;
               signal = SELL;
               break;
            }
         }
      } else {
         change = 0.0;
      }
      Signal out = NEUTRAL;
      if (signal == BUY) {
         Strategy currentBoughtStrategy = boughtStrategy;
         try {
            boughtStrategy = strategy;
            if (buy(candle, strategy)) {
               tradeDuration = -1L;
               boughtPrice = candle.close;
               tradeStartTime = System.currentTimeMillis();
               for (int i = 0; i < monitors.length; i++) {
                  monitors[i].bought();
               }
               currentBoughtStrategy = strategy;
               ticks = 0;
               max = candle.close;
               min = candle.close;
               out = BUY;
            }
         } finally {
            boughtStrategy = currentBoughtStrategy;
         }
      } else if (stopped || signal == SELL) {
         if (sell(candle)) {
            this.tradeDuration = System.currentTimeMillis() - tradeStartTime;
            for (int i = 0; i < monitors.length; i++) {
               monitors[i].sold();
            }
            tradeStartTime = 0L;
            boughtStrategy = null;
            out = SELL;
         }
      }
      return out;
   }

   private boolean buy(Candle candle, Strategy strategy) {
      for (int i = 0; i < monitors.length; i++) {
         if (monitors[i].discardBuy(strategy)) {
            return false;
         }
      }
      if (tradingManager.hasAssets(candle)) {
         log.trace("Discarding buy of {} @ {}: already have assets ({} units)", tradingManager.getSymbol(), candle.close, tradingManager.getAssets());
         return false;
      }
      if (tradingManager.waitingForBuyOrderToFill()) {
         tradingManager.cancelStaleOrdersFor(this);
         if (tradingManager.waitingForBuyOrderToFill()) {
            log.trace("Discarding buy of {} @ {}: got buy order waiting to be filled", tradingManager.getSymbol(), candle.close);
            return false;
         }
      }
      if (tradingManager.isBuyLocked()) {
         log.trace("Discarding buy of {} @ {}: purchase order already being processed", tradingManager.getSymbol(), candle.close);
         return false;
      }
      double amountToSpend = tradingManager.allocateFunds();
      final double minimum = getPriceDetails().getMinimumOrderAmount(candle.close);
      if (amountToSpend * candle.close <= minimum) {
         tradingManager.cancelStaleOrdersFor(this);
         amountToSpend = tradingManager.allocateFunds() / candle.close;
         if (amountToSpend * candle.close <= minimum) {
            if (tradingManager.exitExistingPositions(tradingManager.assetSymbol, candle)) {
               tradingManager.updateBalances();
               return true;
            } else {
               tradingManager.updateBalances();
               amountToSpend = tradingManager.allocateFunds() / candle.close;
               if (amountToSpend * candle.close <= minimum) {
                  log.trace("Discarding buy of {} @ {}: not enough funds to allocate (${})", getSymbol(), candle.close, tradingManager.getCash());
                  return false;
               }
            }
         }
      }
      if (tradingManager.buy(amountToSpend / candle.close)) {
         tradingManager.updateBalances();
         return true;
      }
      log.trace("Could not buy {} @ {}", tradingManager.getSymbol(), candle.close);
      return false;
   }

   private void updateBoughtPrice(Candle candle, double unitPrice, double quantity) {
      if (candle != null) {
         if (boughtPrices.isEmpty()) {
            this.max = this.min = unitPrice;
         }
         this.boughtPrices.compute(unitPrice, (p, q) -> q == null ? quantity : q + quantity);
         double quantities = 0.0;
         double prices = 0.0;
         for (var e : boughtPrices.entrySet()) {
            double price = e.getKey();
            double qty = e.getValue();
            prices += price * qty;
            quantities += qty;
         }
         this.boughtPrice = prices / quantities;
      }
   }

   private boolean canSell(Candle candle) {
      if (!tradingManager.hasAssets(candle)) {
         log.trace("Ignoring sell signal of {}: no assets ({})", getSymbol(), tradingManager.getAssets());
         return false;
      }
      for (int i = 0; i < monitors.length; i++) {
         if (!monitors[i].allowExit()) {
            return false;
         }
      }
      return true;
   }

   private boolean sell(Candle candle) {
      if (canSell(candle)) {
         for (int i = 0; i < monitors.length; i++) {
            if (monitors[i].discardSell(candle)) {
               return false;
            }
         }
         if (tradingManager.sell(tradingManager.getAssets())) {
            tradingManager.updateBalances();
            return true;
         }
      }
      return false;
   }

   private double priceChangePct(double price) {
      if (boughtPrice == 0.0) {
         boughtPrice = price;
      }
      double out = (price / boughtPrice) - 1.0;
      return out * 100;
   }

   /**
    * Returns the formatted most positive change percentage reached during the current (or latest) trade.
    *
    * @return the maximum change percentage , formatted as {@code #,##0.00%}
    */
   public String getFormattedMaxChangePct() {
      return getPct(maxChange);
   }

   /**
    * Returns the maximum closing price reached during the current (or latest) trade.
    *
    * @return the maximum closing price recorded for the traded symbol since opening latest the position.
    */
   public double getMaxPrice() {
      return max;
   }

   /**
    * Returns the minimum closing price reached during the current (or latest) trade.
    *
    * @return the minimum closing price recorded for the traded symbol since opening latest the position.
    */
   public double getMinPrice() {
      return min;
   }

   /**
    * Returns the formatted most negative change percentage reached during the current (or latest) trade.
    *
    * @return the maximum change percentage for the trade symbol, formatted as {@code #,##0.00%}
    */
   public String getFormattedMinChangePct() {
      return getPct(minChange);
   }

   /**
    * Returns the formatted current price change percentage of the current (or latest) trade.
    *
    * @return the current change percentage, formatted as {@code #,##0.00%}
    */
   public String getFormattedPriceChangePct() {
      return getPct(getChange());
   }

   /**
    * Returns the formatted current price change percentage of the {@link #getBoughtPrice()} relative to a given amount.
    *
    * @param paid the actual amount spent on an {@link Order}
    * @return the change percentage, formatted as {@code #,##0.00%}
    */
   public String getFormattedPriceChangePct(BigDecimal paid) {
      return getFormattedPriceChangePct(paid.doubleValue());
   }

   private String getFormattedPriceChangePct(double paid) {
      double pct = priceChangePct(paid);
      return getPct(pct);
   }

   private String getPct(double percentage) {
      return Candle.CHANGE_FORMAT.get().format(percentage / 100.0);
   }

   /**
    * Returns the current price change percentage of the current (or latest) trade
    *
    * @return the current change percentage, where 100% change is returned as {@code 100.0}
    */
   public double getPriceChangePct() {
      return getChange();
   }

   /**
    * Returns the estimated total funds in the reference currency (given by {@link Client#getReferenceCurrency()})
    *
    * @return the estimate net worth of the account.
    */
   public double holdings() {
      return tradingManager.getTotalFundsInReferenceCurrency();
   }

   /**
    * Returns a description detailing why the latest trade was closed. Typically populated from {@link StrategyMonitor#handleStop(Signal, Strategy)} when a trade is stopped without a {@code SELL}
    * signal.
    *
    * @return the reason for exiting the latest trade.
    */
   public String exitReason() {
      return exitReason;
   }

   /**
    * Returns the number of ticks processed since the latest trade opened.
    *
    * @return the count of ticks registered so far for the trade.
    */
   public int tradeLength() {
      return ticks;
   }

   /**
    * Return the latest candle received by this {@code trader}
    *
    * @return the most recent tick received for the symbol being traded.
    */
   public Candle getCandle() {
      return latestCandle;
   }

   /**
    * Returns the price paid when the current (or latest) trade was opened.
    *
    * @return the unit amount paid at the time the trade was opened.
    */
   public double getBoughtPrice() {
      return boughtPrice;
   }

   public void notifySimulationEnd() {
      tradingManager.notifySimulationEnd();
   }

   void notifyTrade(Candle c, Order response) {
      try {
         double unitPrice = response.getPrice().doubleValue();
         if (unitPrice <= 0.0 && c != null) {
            unitPrice = c.close;
         }
         try {
            tradingManager.notifyOrderSubmitted(response);
         } finally {
            if (response.getSide() == Order.Side.BUY) {
               updateBoughtPrice(c, unitPrice, response.getQuantity().doubleValue());
            }
         }
      } catch (Exception e) {
         log.error("Error notifying of " + response.getSide() + " trade: " + response, e);
      }
   }

   /**
    * Tries to exit a current trade to immediately buy into another instrument. In cases where it's supported, such as currencies and crypto, a "direct" switch will be executed to save trading fees;
    * i.e. if there's an open position on BTCUSDT, and the exit symbol is ETH, a single SELL order of symbol BTCETH will be executed, selling BTC to open a position in ETH. If no compatible trading
    * symbols exists, or the market operates just with stocks, the current position will be sold in order to make funds available for buying into the next instrument. Using the previous example, BTC
    * would be sold back into USDT, and another BUY order would be made using the USDT funds to buy ETH. If any call {@link StrategyMonitor#allowTradeSwitch(String, Candle, String)} evaluates to
    * {@code false}, the "switch" operation will be cancelled, otherwise the current open position will be closed to release funds for the trader to BUY into the exitSymbol.
    *
    * @param exitSymbol the new instrument to be bought into using the funds allocated by the current open order (in {@link #getSymbol()}.
    * @param candle the latest candle of the exitSymbol that's been received from the exchange.
    * @param candleTicker the ticker of the received candle (not the same as {@link #getSymbol()}).
    * @return a flag indicating whether an order for a direct switch was opened. If {@code true}, the trader won't try to create a BUY order for the given exitSymbol. When {@code false} the trader
    *         will try to buy into the exitSymbol regardless of whether the current position was closed or not.
    */
   boolean switchTo(String exitSymbol, Candle candle, String candleTicker) {
      try {
         if (exitSymbol.equals(tradingManager.fundSymbol) || exitSymbol.equals(tradingManager.assetSymbol)) {
            return false;
         }
         if (tradingManager.hasAssets(candle)) {
            boolean canExit = monitors.length > 0;
            for (int i = 0; i < monitors.length; i++) {
               canExit &= monitors[i].allowTradeSwitch(exitSymbol, candle, candleTicker);
            }
            if (canExit) { // if true, IT WILL SELL
               // boolean toExit = tradingManager.isDirectSwitchSupported(getAssetSymbol(), exitSymbol);
               // boolean fromExit = tradingManager.isDirectSwitchSupported(exitSymbol, getAssetSymbol());
               exitReason = "exit to buy " + exitSymbol;
               /*
                * if (toExit || fromExit) { // Need to find out the value of the new symbol using the same "cash" unit. Example: // Sell ADA (ADAUSDT) to buy BTC (ADABTC). We need to know how much a
                * BTC is worth in USDT to // find out how much ADA to sell on the ADABTC market. String targetTicker = toExit ? getAssetSymbol() + exitSymbol : exitSymbol + getAssetSymbol(); if
                * (tradingManager.switchTo(targetTicker, toExit ? SELL : BUY, exitSymbol)) { boughtPrices.clear(); return true; } else if (tradingManager.sell(tradingManager.getAssets())) {
                * boughtPrices.clear(); } } else
                */
               if (tradingManager.sell(tradingManager.getAssets())) {
                  boughtPrices.clear();
               }
            }
         }
      } catch (Exception e) {
         log.error("Could not exit trade from " + tradingManager.assetSymbol + " to " + exitSymbol, e);
      }
      // could not sell directly to exit symbol. Might have sold or did not sell at all. In either case caller will try to BUY.
      return false;
   }

   /**
    * Returns the last closing price of the symbol being traded by this {@code Trader}
    *
    * @return the {@link Candle#close} of the latest candle (returned via {@link #getCandle()}.
    */
   public double getLastClosingPrice() {
      if (latestCandle == null) {
         return 0.0;
      }
      return latestCandle.close;
   }

   public double getAssetQuantity() {
      return tradingManager.getAssets();
   }

   public String getSymbol() {
      return tradingManager.getSymbol();
   }

   public String getAssetSymbol() {
      return tradingManager.getAssetSymbol();
   }

   public String getFundSymbol() {
      return tradingManager.getFundSymbol();
   }

   public int getTicks() {
      return ticks;
   }

   public long getTradeDuration() {
      if (tradeDuration != -1) {
         return tradeDuration;
      }
      if (tradeStartTime == 0L) {
         return 0L;
      }
      return System.currentTimeMillis() - tradeStartTime;
   }

   public String getFormattedTradeDuration() {
      return TimeInterval.getFormattedDuration(getTradeDuration());
   }

   public String getFormattedTradeLength() {
      return TimeInterval.getFormattedDuration(tradeDuration);
   }

   public double getMinChange() {
      return boughtPrice > 0.0 ? minChange : 0.0;
   }

   public double getMaxChange() {
      return boughtPrice > 0.0 ? maxChange : 0.0;
   }

   public double getChange() {
      // need to test bought price here. It will only be valid after the buy order fills.
      return boughtPrice > 0.0 ? change : 0.0;
   }

   public Strategy getStrategy() {
      return boughtStrategy;
   }

   public String getReferenceCurrencySymbol() {
      return tradingManager.getReferenceCurrencySymbol();
   }

   public TradingFees getTradingFees() {
      return tradingManager.getTradingFees();
   }

   public double getBreakEvenChange() {
      return getBreakEvenChange(getBoughtPrice());
   }

   public double getBreakEvenChange(double amount) {
      return tradingManager.getTradingFees().getBreakEvenChange(amount);
   }

   public double getBreakEvenAmount(double amount) {
      return tradingManager.getTradingFees().getBreakEvenAmount(amount);
   }

   public double getTotalFundsInReferenceCurrency() {
      return tradingManager.getTotalFundsInReferenceCurrency();
   }

   public double getTotalFundsIn(String symbol) {
      return tradingManager.getTotalFundsIn(symbol);
   }

   public StrategyMonitor[] getMonitors() {
      return monitors;
   }

   public boolean stopped() {
      return stopped;
   }

   public SymbolPriceDetails getPriceDetails() {
      return tradingManager.getPriceDetails();
   }

   public void setExitReason(String exitReason) {
      this.exitReason = exitReason;
   }

   public String toString() {
      return "Trader{" + getSymbol() + "}";
   }
}
