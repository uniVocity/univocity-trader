package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;

public class BinanceTrader extends LiveTrader<Candlestick, BinanceClientConfiguration> {

	public BinanceTrader(TimeInterval tickInterval) {
		super(new BinanceExchange(), tickInterval);
	}
}
