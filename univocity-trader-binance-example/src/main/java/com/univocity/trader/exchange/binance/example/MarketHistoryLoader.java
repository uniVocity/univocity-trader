package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.indicators.base.*;
import org.springframework.jdbc.datasource.*;

import java.time.*;
import java.time.temporal.*;

public class MarketHistoryLoader {

	static String[][] ALL_PAIRS = new String[][]{
			new String[]{"BTC", "USDT"}
//			, new String[]{"ADA", "USDT"}
//			, new String[]{"LTC", "USDT"}
//			, new String[]{"XRP", "USDT"}
//			, new String[]{"ETH", "USDT"}
//			, new String[]{"BNB", "USDT"}
//			, new String[]{"EOS", "USDT"}
//			, new String[]{"LINK", "USDT"}
//			, new String[]{"NEO", "USDT"}
//			, new String[]{"XLM", "USDT"}
//			, new String[]{"XMR", "USDT"}
//			, new String[]{"IOTA", "USDT"}
//			, new String[]{"NANO", "USDT"}
//			, new String[]{"BAT", "USDT"}
//			, new String[]{"VET", "USDT"}
//			, new String[]{"BCHABC", "USDT"}
//			, new String[]{"ETC", "USDT"}
//			, new String[]{"TRX", "USDT"}
//			, new String[]{"MATIC", "USDT"}
//			, new String[]{"DUSK", "USDT"}
//			, new String[]{"ALGO", "USDT"}
//			, new String[]{"CHZ", "USDT"}
//			, new String[]{"BTT", "USDT"}
//			, new String[]{"ATOM", "USDT"}
//			, new String[]{"ZEC", "USDT"}
//			, new String[]{"DASH", "USDT"}
//			, new String[]{"QTUM", "USDT"}
//			, new String[]{"ICX", "USDT"}
//			, new String[]{"ONT", "USDT"}
//			, new String[]{"FET", "USDT"}
//			, new String[]{"WIN", "USDT"}
//			, new String[]{"DOCK", "USDT"}
//			, new String[]{"WAVES", "USDT"}
//			, new String[]{"CHZ", "USDT"}
//			, new String[]{"COCOS", "USDT"}
//			, new String[]{"CELR", "USDT"}
//			, new String[]{"ONE", "USDT"}
//			, new String[]{"PERL", "USDT"}
//			, new String[]{"IOST", "USDT"}
//			, new String[]{"ERD", "USDT"}
	};

	public static void main(String... args) {
		//TODO: configure your database connection here.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		SingleConnectionDataSource ds = new SingleConnectionDataSource();
		ds.setUrl("jdbc:mysql://localhost:3306/trading?autoReconnect=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull");
		ds.setUsername("root"); // or the appropriate user name
		ds.setPassword("YOUR-PASSWORD"); // omit this line if you have no password
		ds.setSuppressClose(true);

		//CandleRepository manages everything for us.
		CandleRepository.setDataSource(ds);

		BinanceExchange exchangeApi = new BinanceExchange();
		final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
		for (String[] pair : ALL_PAIRS) {
			String symbol = pair[0] + pair[1];
			CandleRepository.fillHistoryGaps(exchangeApi, symbol, start, TimeInterval.minutes(1));
		}
		System.exit(0);
	}
}
