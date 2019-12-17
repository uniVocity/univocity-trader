package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;

public class BinanceTrader extends LiveTrader<Candlestick, Account> {

	public BinanceTrader(TimeInterval tickInterval) {
		super(new BinanceExchange(), tickInterval);
	}
}
