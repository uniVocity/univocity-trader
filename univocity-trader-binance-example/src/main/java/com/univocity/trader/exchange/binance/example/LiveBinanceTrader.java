package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;

import java.math.*;
import java.time.*;

import static com.univocity.trader.indicators.Signal.*;

public class LiveBinanceTrader {

	private static final MailSenderConfig getEmailConfig() {
		MailSenderConfig out = new MailSenderConfig();

		out.setReplyToAddress("dev@univocity.com");
		out.setSmtpHost("smtp.gmail.com");
		out.setSmtpTlsSsl(true);
		out.setSmtpPort(587);
		out.setSmtpUsername("<YOU>@gmail.com");
		out.setSmtpPassword("<YOUR SMTP PASSWORD>".toCharArray());
		out.setSmtpSender("<YOU>>@gmail.com");

		return out;
	}

	public static void main(String... args) {
		//TODO: configure your database connection as needed.
		//DataSource ds = ?
		//CandleRepository.setDataSource(ds);

		BinanceTrader binance = new BinanceTrader(TimeInterval.minutes(1), getEmailConfig());

		String apiKey = "<YOUR BINANCE API KEY>";
		String secret = "<YOUR BINANCE API SECRET>";

		Client client = binance.addClient("<YOUR E-MAIL>", ZoneId.systemDefault(), "USDT", apiKey, secret);
		client.tradeWith("BTC", "ETH", "XRP", "ADA");

		client.strategies().add(ExampleStrategy::new);
		client.monitors().add(ExampleStrategyMonitor::new);

		client.account().maximumInvestmentAmountPerAsset(20);
		client.account().setOrderManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
				switch (order.getSide()) {
					case BUY:
						order.setPrice(order.getPrice().multiply(new BigDecimal("0.9"))); //10% less
						break;
					case SELL:
						order.setPrice(order.getPrice().multiply(new BigDecimal("1.1"))); //10% more
				}
			}
		});

		client.listeners().add(new OrderExecutionToLog());
		binance.run();

	}

}
