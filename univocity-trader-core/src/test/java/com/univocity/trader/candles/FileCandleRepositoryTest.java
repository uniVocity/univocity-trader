package com.univocity.trader.candles;

import com.univocity.trader.simulation.*;
import com.univocity.trader.utils.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class FileCandleRepositoryTest {


	@Test
	public void testCandleLoadingProcess() {
		FileCandleRepository repository = new FileCandleRepository(new RepositoryDir(FileBasedSimulation.pathToRepositoryDir()), FileBasedSimulation.csvFileFormat());

		Enumeration<Candle> candles = repository.iterate("BTCUSD", null, null, false);
		Candle first = null;
		Candle last = null;
		int count = 0;
		while (candles.hasMoreElements()) {
			Candle c = candles.nextElement();
			if (c != null) {
				last = c;
				count++;
			}
			if (first == null) {
				first = last;
			}
		}

		assertEquals(count, 365);

		String firstStr = "Jan 22 00:00 | O(8,744.21093800), C(8,680.87597700),  H(8,792.99414100), L(8,636.74707000), V(22,600,204,050.00000000)";
		assertNotNull(first);
		assertEquals(firstStr, first.toString());

		String lastStr = "Jan 22 00:00 | O(30,483.74218800), C(30,861.99414100),  H(31,960.31054700), L(28,953.37304700), V(94,849,540,096.00000000)";
		assertNotNull(last);
		assertEquals(lastStr, last.toString());

		first = repository.firstCandle("BTCUSD");
		assertNotNull(first);
		assertEquals(firstStr, first.toString());

		last = repository.lastCandle("BTCUSD");
		assertNotNull(last);
		assertEquals(lastStr, last.toString());
	}

}