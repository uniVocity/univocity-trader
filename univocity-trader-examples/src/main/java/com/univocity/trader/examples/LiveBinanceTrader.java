package com.univocity.trader.examples;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.notification.*;

import java.math.*;

public class LiveBinanceTrader {

	public static void main(String... args) {
		Binance.Trader trader = Binance.trader();

//		TODO: configure your database connection as needed. By default MySQL will be used
//		trader.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

//		If you want to receive e-mail notifications each time an order is submitted to the exchange, configure your e-mail sender
		trader.configure().mailSender()
				.replyToAddress("dev@univocity.com")
				.smtpHost("smtp.gmail.com")
				.smtpSSL(true)
				.smtpPort(587)
				.smtpUsername("<YOU>@gmail.com")
				.smtpPassword("<YOUR SMTP PASSWORD>")
				.smtpSender("<YOU>>@gmail.com");

		//set an e-mail and timezone here to get notifications to your e-mail every time a BUY or SELL happens.
		//the timezone is required if you want to host this in a server outside of your local timezone
		//so the time a trade happens will come to you in your local time and not the server time
		Account account = trader.configure().account()
				.email("<YOUR E-MAIL")
				.timeZone("system")
				.referenceCurrency("USDT")
				.apiKey("<YOUR BINANCE API KEY>")
				.secret("<YOUR BINANCE API SECRET>");

		account.strategies().add(ExampleStrategy::new);
		account.monitors().add(ExampleStrategyMonitor::new);
		account.listeners().add(new OrderExecutionToLog());

//		never invest more than 20 USDT on anything
		account
				.tradeWith("BTC", "ETH", "XRP", "ADA")
				.maximumInvestmentAmountPerAsset(20)
		;

//		overrides the default order manager submit orders that likely won't be filled so you can see what the program does.
		account.orderManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
				switch (order.getSide()) {
					case BUY:
						order.setPrice(order.getPrice() * 0.9); //10% less
						break;
					case SELL:
						order.setPrice(order.getPrice() * 1.1); //10% more
				}
			}
		});

//		Begin trading
		trader.run();
	}
}

