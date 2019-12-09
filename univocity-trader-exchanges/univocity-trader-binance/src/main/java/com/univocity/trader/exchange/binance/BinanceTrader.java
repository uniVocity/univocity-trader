package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;

public class BinanceTrader extends LiveTrader<Candlestick> {

	public BinanceTrader(TimeInterval tickInterval) {
		this(tickInterval, null);
	}

	public BinanceTrader(TimeInterval tickInterval, MailSenderConfig mailSenderConfig) {
		super(new BinanceExchange(), tickInterval, mailSenderConfig);
	}
}
