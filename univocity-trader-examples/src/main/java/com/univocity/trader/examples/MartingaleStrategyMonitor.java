package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.util.*;

public class MartingaleStrategyMonitor extends StrategyMonitor {

	private static final Logger log = LoggerFactory.getLogger(MartingaleStrategyMonitor.class);
	private long lastBuy;

	private double doublePercentage = -2.0;

	@Override
	protected Set<Indicator> getAllIndicators() {
		return null;
	}

	private boolean buyToday() {
		return trader.latestCandle().closeTime - lastBuy >= TimeInterval.days(1).ms;
	}

	@Override
	public boolean discardBuy(Strategy strategy) {
		//don't buy via strategy if more than 100 bucks invested.
		if (trader.assetQuantity() * trader.lastClosingPrice() > 100.0) {
			return true;
		}

		if (!buyToday()) {
			return true;
		}
		lastBuy = trader.latestCandle().closeTime;
		return false;
	}

	@Override
	public String handleStop(Trade trade, Signal signal, Strategy strategy) {
		if (trade.priceChangePct() > 3.0) {
			return "take profit";
		}

//		if (!buyToday() && trader.priceChangePct() < doublePercentage - 2.0) { //lost another 2.0 after buying again in the same day. Let go
//			return "stop loss";
//		}
		return null;
	}

	@Override
	public void worstLoss(Trade trade, double change) {
		if (!buyToday()) {
			return;
		}

		//for every 2% lost, double position.
		if (change < doublePercentage) {
			doublePercentage = change - 2.0;

			double amount = trader.totalFundsInReferenceCurrency();
			if (amount < 10.0) {
				log.warn("No more funds to allocate for doubling position in " + trader.symbol());
				return;
			}
			double maxQuantity = amount / trader.lastClosingPrice();

			double quantity = Math.min(trader.assetQuantity(), maxQuantity);

			Order order = trader.submitOrder(Order.Type.LIMIT, Order.Side.BUY, quantity);
			if (order != null && !order.isCancelled()) {
				lastBuy = trader.latestCandle().closeTime;
				log.info(">>> Lost {} in {}, doubling position with {}", trade.formattedPriceChangePct(), trader.symbol(), order);
			}
		}
	}

	@Override
	public boolean allowExit(Trade trade) {
		if (trade.priceChangePct() > 2.0) {
			doublePercentage = -2.0; //reset
			return true;
		} else if (!trade.stopped()) {
			log.info("Preventing sell of {} under 2% profit. Current price: {} ({})", trade.symbol(), trade.lastClosingPrice(), trade.formattedPriceChangePct());
			return false;
		} else {
			return true;
		}
	}
}
