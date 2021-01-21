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

import java.io.*;
import java.util.*;

import static ch.qos.logback.classic.Level.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.slf4j.Logger.*;
import static org.slf4j.LoggerFactory.*;

public class MockExchangeTest {

	private static Strategy strategy() {
		return new Strategy() {
			@Override
			public Signal getSignal(Candle candle, Context context) {
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
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				super.prepareOrder(book, order, context);
				t[0] = context.trader();
			}
		};
		trader.configure().account().orderManager(om);

		if (listener != null) {
			trader.configure().account().listeners().add(listener);
		}
		return trader;
	}

	@Test
//	@Ignore
	public void testExchangeWithRandomValues() throws Exception {
		((Logger) getLogger(ROOT_LOGGER_NAME)).setLevel(TRACE);
		Trader[] t = new Trader[1];
		OrderExecutionToCsv csv = new OrderExecutionToCsv("debug");

		getExchangeLiveTrader(null, t, csv).run();

		Thread.sleep(1500);
		csv.simulationEnded(t[0], null);
	}

	@Test
//	@Ignore
	public void testExchangeWithFixedValues() throws Exception{
		double[] values = new double[]{
				4.125326609204672, 4.22981664028250082, 5.655281028532778, 5.02290469160646, 5.249559930965637, 4.2996318674523568, 4.379173514488954, 4.806110096763411
				, 4.560653648869072, 5.27328121546796, 5.424995186976494, 4.5707920138112896, 5.69997600235952, 5.216677714520097, 4.408959331607647
				, 5.927190114350763, 5.764947770383927, 5.013266729421126, 4.255788682912335, 4.5133477212327464, 4.9125701219281444, 4.18801627662217
				, 4.9727338760123145, 4.9404061506289596, 4.11053312187002229, 4.0408585274746773, 4.783740234236968, 4.17643923949326012, 4.842655622609823
				, 4.2533823007598779, 4.787017750193766, 5.01366132549878, 5.289128539210227, 4.4375985379917433, 5.490945896832793, 5.138741639693412
				, 4.12328481242194211, 5.516870264157543, 5.379674986510635, 4.139767843780634, 4.9905135642745198, 4.614891410758009, 5.222135871550176, 5.0386598200145345
				, 4.6106173121470237, 4.8746723905884823, 4.8715323437521414, 5.471409068650496, 5.479746505792662, 5.377251797870828, 4.2779450454993766, 4.8206703372797295
				, 4.8056955262641603, 4.43993139667319325, 4.525292775191951, 5.2925703215659325, 4.193082549261364, 5.843522099406282
				, 5.626179662289607, 4.684185369999952, 5.412071267687864, 4.2560858773279344, 5.803606314353895, 5.62835580549536
				, 5.574372390946888, 5.922484986807622, 4.507373022083929, 5.485779875804155, 4.7069699506121436, 4.18661301579726
				, 4.5504889924105276, 5.635832299593108, 5.353180408198164, 4.895447677868593, 5.686723239184523, 5.7890312277864675, 5.452524841960884
				, 4.0547644219157033, 4.796333173691741, 4.021352394560164, 4.352736831767709, 4.6124337320015, 4.70302332278861, 4.801992961009671
				, 4.2073428786786624, 5.452074282331463, 5.118746022053272, 4.626973568039919, 5.558247308017779
				, 5.644576714798736, 5.901962856502174, 4.9785771021367156, 4.9082826887734514, 5.02428962837078, 4.24625042646691964, 4.2649091491112807
				, 5.663181982046959, 4.267125475761247, 4.3824415668249779, 5.474919915590425, 4.83453320430184, 4.2962093530300025, 4.5002403948258416, 5.708494358326413
				, 4.7741330483026674, 4.16496248232977, 4.47407678326718217, 5.682430557442105, 5.676204099115016, 5.504766472244214, 5.083781271774761, 5.726499876506807
				, 5.190705263653555, 4.799137751131479, 4.4883537007839887, 4.6275110089087743, 5.731801201270694, 5.912006263364528, 5.332351509227395
				, 5.4469932246998995, 4.135399800296746, 4.4158774818688271, 4.954501934327308, 4.3804087604242665, 4.355387742730079, 5.177746048970753, 5.986190035831635
				, 4.038135688266902, 4.388042197225758, 4.145319736525694, 5.503509074980624, 4.1668178426598277, 5.189402052184318, 4.2517227417575967, 5.62874747950874
				, 5.343909692997675
		};

		List<Candle> candles = new ArrayList<>();
		long time = 0;
		for (double v : values) {
			candles.add(new Candle(time, time + MINUTE.ms, v, v, v, v, 100));
			time += MINUTE.ms;
		}
		Map<String, List<Candle>> input = new HashMap<>();
		input.put("ADAUSDT", candles);

		((Logger) getLogger(ROOT_LOGGER_NAME)).setLevel(TRACE);
		Trader[] t = new Trader[1];
		OrderExecutionToCsv csv = new OrderExecutionToCsv("debug");

		getExchangeLiveTrader(input, t, csv).run();

		while (!candles.isEmpty()) {
			Thread.yield();
		}
		Thread.yield();

		csv.simulationEnded(t[0], null);
	}
}
