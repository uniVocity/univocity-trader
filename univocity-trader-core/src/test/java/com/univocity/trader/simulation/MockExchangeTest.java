package com.univocity.trader.simulation;

import ch.qos.logback.classic.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import org.junit.*;

import java.util.*;

import static ch.qos.logback.classic.Level.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.slf4j.Logger.*;
import static org.slf4j.LoggerFactory.*;

public class MockExchangeTest {

	private static Strategy strategy() {
		return new Strategy() {
			@Override
			public Signal getSignal(Candle candle) {
				return candle.close > 5 ? Signal.SELL : Signal.BUY;
			}
		};
	}

	private MockExchange.Trader getExchangeLiveTrader(Map<String, List<Candle>> candles, Trader[] t, OrderListener listener) {
		MockExchange.Trader trader = MockExchange.trader(candles);

		trader.configure().account()
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "USDT")
				.strategies()
				.add(MockExchangeTest::strategy);

		OrderManager om = new DefaultOrderManager() {
			@Override
			public TimeInterval getOrderUpdateFrequency() {
				return TimeInterval.millis(1);
			}

			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Trader trader) {
				super.prepareOrder(priceDetails, book, order, trader);
				t[0] = trader;
			}
		};
		trader.configure().account().orderManager(om);

		if (listener != null) {
			trader.configure().account().listeners().add(listener);
		}
		return trader;
	}

	@Test
	@Ignore
	public void testExchangeWithRandomValues() throws Exception {
		((Logger) getLogger(ROOT_LOGGER_NAME)).setLevel(TRACE);
		Trader[] t = new Trader[1];
		OrderExecutionToCsv csv = new OrderExecutionToCsv("debug");

		getExchangeLiveTrader(null, t, csv).run();

		Thread.sleep(1500);
		csv.simulationEnded(t[0], null);
	}

	@Test
	@Ignore
	public void testExchangeWithFixedValues() {
		double[] values = new double[]{
				2.125326609204672, 0.22981664028250082, 7.655281028532778, 9.02290469160646, 7.249559930965637, 0.2996318674523568, 4.379173514488954, 3.806110096763411
				, 4.560653648869072, 8.27328121546796, 8.424995186976494, 1.5707920138112896, 8.69997600235952, 9.216677714520097, 4.408959331607647
				, 8.927190114350763, 6.764947770383927, 8.013266729421126, 3.255788682912335, 0.5133477212327464, 1.9125701219281444, 2.18801627662217
				, 2.9727338760123145, 0.9404061506289596, 0.11053312187002229, 2.0408585274746773, 4.783740234236968, 0.17643923949326012, 3.842655622609823
				, 0.2533823007598779, 4.787017750193766, 8.01366132549878, 8.289128539210227, 1.4375985379917433, 9.490945896832793, 6.138741639693412
				, 0.12328481242194211, 8.516870264157543, 7.379674986510635, 4.139767843780634, 1.9905135642745198, 3.614891410758009, 6.222135871550176, 7.0386598200145345
				, 0.6106173121470237, 0.8746723905884823, 1.8715323437521414, 5.471409068650496, 7.479746505792662, 5.377251797870828, 2.2779450454993766, 1.8206703372797295
				, 2.8056955262641603, 0.43993139667319325, 1.525292775191951, 6.2925703215659325, 2.193082549261364, 7.843522099406282
				, 6.626179662289607, 3.684185369999952, 9.412071267687864, 3.2560858773279344, 5.803606314353895, 9.62835580549536
				, 9.574372390946888, 6.922484986807622, 4.507373022083929, 6.485779875804155, 3.7069699506121436, 2.18661301579726
				, 3.5504889924105276, 9.635832299593108, 7.353180408198164, 3.895447677868593, 5.686723239184523, 5.7890312277864675, 5.452524841960884
				, 3.0547644219157033, 1.796333173691741, 4.021352394560164, 1.352736831767709, 2.6124337320015, 2.70302332278861, 0.801992961009671
				, 0.2073428786786624, 7.452074282331463, 5.118746022053272, 4.626973568039919, 7.558247308017779
				, 8.644576714798736, 6.901962856502174, 2.9785771021367156, 2.9082826887734514, 7.02428962837078, 0.24625042646691964, 1.2649091491112807
				, 8.663181982046959, 4.267125475761247, 1.3824415668249779, 9.474919915590425, 4.83453320430184, 0.2962093530300025, 0.5002403948258416, 7.708494358326413
				, 1.7741330483026674, 4.16496248232977, 0.47407678326718217, 8.682430557442105, 7.676204099115016, 8.504766472244214, 9.083781271774761, 9.726499876506807
				, 6.190705263653555, 4.799137751131479, 1.4883537007839887, 0.6275110089087743, 6.731801201270694, 5.912006263364528, 9.332351509227395
				, 5.4469932246998995, 4.135399800296746, 0.4158774818688271, 4.954501934327308, 1.3804087604242665, 4.355387742730079, 6.177746048970753, 7.986190035831635
				, 2.038135688266902, 3.388042197225758, 4.145319736525694, 9.503509074980624, 0.1668178426598277, 8.189402052184318, 3.2517227417575967, 6.62874747950874
				, 7.343909692997675
		};

		List<Candle> candles = new ArrayList<>();
		long time = 0;
		for (double v : values) {
			candles.add(new Candle(time, time + MINUTE.ms, v, v, v, v, 100));
		}
		Map<String, List<Candle>> input = new HashMap<>();
		input.put("ADAUSDT", candles);

		((Logger) getLogger(ROOT_LOGGER_NAME)).setLevel(TRACE);
		Trader[] t = new Trader[1];
		OrderExecutionToCsv csv = new OrderExecutionToCsv("debug");

		getExchangeLiveTrader(input, t, new OrderExecutionToLog()).run();

		while(!candles.isEmpty()){
			Thread.yield();
		}
		csv.simulationEnded(t[0], null);
	}
}
