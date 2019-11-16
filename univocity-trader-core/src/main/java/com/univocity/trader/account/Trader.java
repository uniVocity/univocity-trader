package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;

import static com.univocity.trader.indicators.Signal.*;
import static com.univocity.trader.utils.NewInstances.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
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

	private Candle prev;
	private Strategy boughtStrategy;
	private Parameters parameters;

	private final boolean allowMixedStrategies;

	public Trader(TradingManager tradingManager, InstancesProvider<StrategyMonitor> monitors, Parameters params) {
		this.parameters = params;
		this.tradingManager = tradingManager;
		this.tradingManager.trader = this;

		this.monitors = monitors == null ? new StrategyMonitor[0] : getInstances(tradingManager.getSymbol(), parameters, monitors, "StrategyMonitor", false);
		boolean allowMixedStrategies = true;
		for (int i = 0; i < this.monitors.length; i++) {
			this.monitors[i].setTrader(this);
			allowMixedStrategies &= this.monitors[i].allowMixedStrategies();
		}
		this.allowMixedStrategies = allowMixedStrategies;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public StrategyMonitor[] getStrategyMonitors() {
		return monitors;
	}

	public Signal trade(Candle candle, Signal signal, Strategy strategy) {
		prev = candle;

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

		Signal out = null;

		if (signal == BUY) {
			Strategy currentBoughtStrategy = boughtStrategy;
			try {
				boughtStrategy = strategy;
				if (buy(candle, strategy)) {
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
			tradingManager.cancelStaleOrders();
			if (tradingManager.waitingForBuyOrderToFill()) {
				log.debug("Discarding buy of {} @ {}: got buy order waiting to be filled", tradingManager.getSymbol(), candle.close);
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
			tradingManager.cancelStaleOrders();
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
			if (!monitors[i].allowSelling()) {
				return false;
			}
		}
		return true;
	}

	private boolean sell(Candle candle) {
		if (canSell(candle)) {
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

	public String getFormattedMaxChangePct() {
		return getPct(maxChange);
	}

	public double getMaxPrice() {
		return max;
	}

	public double getMinPrice() {
		return min;
	}

	public String getFormattedMinChangePct() {
		return getPct(minChange);
	}

	public String getFormattedPriceChangePct() {
		return getPct(getChange());
	}

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

	public double getPriceChangePct() {
		return getChange();
	}

	public double holdings() {
		return tradingManager.getTotalFundsInReferenceCurrency();
	}

	public String exitReason() {
		return exitReason;
	}

	public int tradeLength() {
		return ticks;
	}

	public double tradeMax() {
		return max;
	}

	public double tradeMin() {
		return min;
	}

	public Candle getCandle() {
		return prev;
	}

	public double getBoughtPrice() {
		return boughtPrice;
	}

	void notifyTrade(Candle c, Order response) {
		try {
			double unitPrice = response.getPrice().doubleValue();
			if (unitPrice <= 0.0 && c != null) {
				unitPrice = c.close;
			}
			try {
				tradingManager.notifyTradeExecution(response);
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
	 * Tries to exit a current trade to immediately buy into another instrument.
	 *
	 * In cases where it's supported, such as currencies and crypto, a "direct" switch will be executed to save trading fees;
	 * i.e. if there's an open position on BTCUSDT, and the exit symbol is ETH, a single SELL order of symbol BTCETH
	 * will be executed, selling BTC to open a position in ETH.
	 *
	 * If no compatible trading symbols exists, or the market operates just with stocks, the current position
	 * will be sold in order to make funds available for buying into the next instrument.
	 *
	 * Using the previous example, BTC would be sold back into USDT, and another BUY order would be made using the
	 * USDT funds to buy ETH.
	 *
	 * If any call {@link StrategyMonitor#allowTradeSwitch(String, Candle, String)} evaluates to {@code false}, the "switch" operation will be cancelled,
	 * otherwise the current open position will be closed to release funds for the trader to BUY into the exitSymbol.
	 *
	 * @param exitSymbol   the new instrument to be bought into using the funds allocated by the current open order (in {@link #getSymbol()}.
	 * @param candle       the latest candle of the exitSymbol that's been received from the exchange.
	 * @param candleTicker the ticker of the received candle (not the same as {@link #getSymbol()}).
	 *
	 * @return a flag indicating whether an order for a direct switch was opened. If {@code true}, the trader won't try to create a BUY order
	 * for the given exitSymbol. When {@code false} the trader will try to buy into the exitSymbol regardless of whether the current
	 * position was closed or not.
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

				if (canExit) { //if true, IT WILL SELL
//					boolean toExit = tradingManager.isDirectSwitchSupported(getAssetSymbol(), exitSymbol);
//					boolean fromExit = tradingManager.isDirectSwitchSupported(exitSymbol, getAssetSymbol());
					exitReason = "exit to buy " + exitSymbol;
					/*if (toExit || fromExit) {

//						 Need to find out the value of the new symbol using the same "cash" unit. Example:
//						 Sell ADA (ADAUSDT) to buy BTC (ADABTC). We need to know how much a BTC is worth in USDT to
//						 find out how much ADA to sell on the ADABTC market.


						String targetTicker = toExit ? getAssetSymbol() + exitSymbol : exitSymbol + getAssetSymbol();
						if (tradingManager.switchTo(targetTicker, toExit ? SELL : BUY, exitSymbol)) {
							boughtPrices.clear();
							return true;
						} else if (tradingManager.sell(tradingManager.getAssets())) {
							boughtPrices.clear();
						}

					} else*/
					if (tradingManager.sell(tradingManager.getAssets())) {
						boughtPrices.clear();
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not exit trade from " + tradingManager.assetSymbol + " to " + exitSymbol, e);
		}
		//could not sell directly to exit symbol. Might have sold or did not sell at all. In either case caller will try to BUY.
		return false;
	}

	public double getLastClosingPrice() {
		if (prev == null) {
			return 0.0;
		}
		return prev.close;
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

	public long getTimeSinceTradeOpened() {
		if (tradeStartTime == 0L) {
			return 0L;
		}
		return System.currentTimeMillis() - tradeStartTime;
	}

	public String getFormattedTimeSinceTradeOpened() {
		return TimeInterval.getFormattedDuration(getTimeSinceTradeOpened());
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

	public TradingFees getTradingFees(){
		return tradingManager.getTradingFees();
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

	public StrategyMonitor[] getMonitors(){
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
