package com.univocity.trader.indicators;

import com.univocity.trader.utils.*;
import org.junit.*;

import static junit.framework.TestCase.*;

public class LinearRegressionTest {

	@Test
	public void testLinearRegression() {
		double[] y = new double[]{15, 20, 10, 30, 23, 23, 22, 19, 20, 21};

		LinearRegression r = new LinearRegression();
		for(int i = 0; i < y.length; i++){
			r.update(5);
			r.add(y[i]);
		}

		assertEquals(22.8, r.predict(1), 0.0001);
	}

}