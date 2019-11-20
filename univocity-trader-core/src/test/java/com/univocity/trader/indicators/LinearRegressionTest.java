package com.univocity.trader.indicators;

import com.univocity.trader.utils.*;
import org.junit.*;

import static junit.framework.TestCase.*;

public class LinearRegressionTest {

	@Test
	public void testLinearRegression() {
		double[] y = new double[]{15, 20, 10, 30, 23, 23, 22, 19, 20, 21};

		LinearRegression r = new LinearRegression();
		for (int i = 0; i < y.length; i++) {
			r.update(5);
			r.add(y[i]);
		}

		assertEquals(22.8, r.predict(1), 0.0001);

		assertTrue(r.goingUp(5.0)); //going up 5%? 22.8 > 22.05
		assertFalse(r.goingUp(10.0)); //going up 10%? 22.8 < 23.1

		r.add(16);
		assertEquals(20.7818, r.predict(1), 0.0001);
		assertTrue(r.goingUp(5.0)); //going up 5%? 20.78 > 16.8
		assertTrue(r.goingUp(10.0)); //going up 10%? 20.78 < 17.6


		r.add(13);
		assertEquals(18.3333, r.predict(1), 0.0001);

		r.add(15);
		assertEquals(17.1538, r.predict(1), 0.0001);

		r.add(14);
		assertEquals(15.989, r.predict(1), 0.0001);

		r.add(16);
		assertEquals(15.638, r.predict(1), 0.0001);

		assertTrue(r.goingDown(2.0)); //going up 2%? 15.638 < 15.68
		assertFalse(r.goingDown(5.0)); //going up 5%? 15.638 > 15.2

	}
}