package com.univocity.trader.examples;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.interactivebrokers.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;

import static com.univocity.trader.exchange.interactivebrokers.SecurityType.*;

public class LiveForexTrader {

	public static void main(String... args) {
		InteractiveBrokers.Trader trader = InteractiveBrokers.trader();

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
				.referenceCurrency("GBP");

		account
				.referenceCurrency("GBP")
				.tradeWith(FOREX, "EUR", "GBP");

		account
				.minimumInvestmentAmountPerTrade(50.0);

		//shorting is disabled by default
		account.enableShorting();

		trader.configure().tickInterval(TimeInterval.millis(1));

		account.strategies().add(ScalpingStrategy::new);
		account.monitors().add(ScalpingStrategyMonitor::new);
		account.listeners().add(new OrderExecutionToLog());

		// All orders are submitted as "bracket" orders to open either short or long positions. Each order comes with
		// two "attached" orders on each side to exit the position when:
		// - profit reaches 0.25% or more, exit with a limit order
		// - loss reaches 0.15% or more, exits with a market order.
		account.orderManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
				order.attach(Order.Type.LIMIT, 0.25);
				order.attach(Order.Type.MARKET, -0.15);
			}
		});

//		Begin trading
		trader.run();
	}
}

