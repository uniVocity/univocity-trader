package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.notification.*;

import java.math.*;

public class LiveBinanceTrader {

	public static void main(String... args) {

//		TODO: configure your database connection as needed. The following options are available:

//		(a) Load configuration file
//		Configuration.load();                                //tries to open a univocity-trader.properties file
//		Configuration.loadFromCommandLine(args);		      //opens a file provided via the command line
//		Configuration.load("/path/to/config", "other.file"); //tries to find specific configuration files

//		(b) Configuration code
//		Configuration.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

//		(c) Use your own DataSource implementation:
//		DataSource ds = ?
//		CandleRepository.setDataSource(ds);

		Binance.Trader trader = Binance.liveTrader();

		trader.configure().loadConfigurationFromProperties();

		trader.configure().mailSender()
				.replyToAddress("dev@univocity.com")
				.smtpHost("smtp.gmail.com")
				.smtpSSL(true)
				.smtpPort(587)
				.smtpUsername("<YOU>@gmail.com")
				.smtpPassword("<YOUR SMTP PASSWORD>")
				.smtpSender("<YOU>>@gmail.com");

		Account clientConfig = trader.configure().account()
				.email("<YOUR E-MAIL")
				.timeZone("system")
				.referenceCurrency("USDT")
				.apiKey("<YOUR BINANCE API KEY>")
				.secret("<YOUR BINANCE API SECRET>");

		clientConfig.strategies().add(ExampleStrategy::new);
		clientConfig.monitors().add(ExampleStrategyMonitor::new);
		clientConfig.listeners().add(new OrderExecutionToLog());

		clientConfig.tradeWith("BTC", "ETH", "XRP", "ADA");
		clientConfig.maximumInvestmentAmountPerAsset(20);
		clientConfig.orderManager(new DefaultOrderManager() {
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

		trader.run();

	}
}
