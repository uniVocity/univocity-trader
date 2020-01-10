package com.univocity.trader.chart;

import com.univocity.trader.candles.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class CandleHistoryViewTest {

	double[][] values = new double[][]{
			{44.98, 45.05, 45.17, 44.96, 1},
			{45.05, 45.10, 45.15, 44.99, 2},
			{45.11, 45.19, 45.32, 45.11, 1},
			{45.19, 45.14, 45.25, 45.04, 3},
			{45.12, 45.15, 45.20, 45.10, 1},
			{45.15, 45.14, 45.20, 45.10, 2},
			{45.13, 45.10, 45.16, 45.07, 1},
			{45.12, 45.15, 45.22, 45.10, 5},
			{45.15, 45.22, 45.27, 45.14, 1},
			{45.24, 45.43, 45.45, 45.20, 1},
			{45.43, 45.44, 45.50, 45.39, 1},
			{45.43, 45.55, 45.60, 45.35, 5},
			{45.58, 45.55, 45.61, 45.39, 7},
			{45.45, 45.01, 45.55, 44.80, 6},
			{45.03, 44.23, 45.04, 44.17, 1},
			{44.23, 43.95, 44.29, 43.81, 2},
			{43.91, 43.08, 43.99, 43.08, 1},
			{43.07, 43.55, 43.65, 43.06, 7},
			{43.56, 43.95, 43.99, 43.53, 6},
			{43.93, 44.47, 44.58, 43.93, 1},
	};


	CandleHistory candleHistory;

	protected CandleHistory getCandleHistory() {
		if (candleHistory == null) {
			candleHistory = new CandleHistory();
			addCandles();
		}
		return candleHistory;
	}

	private void addCandles() {
		for (int i = 0; i < values.length; i++) {
			getCandleHistory().addSilently(new Candle(i * 60000, (i + 1) * 60000, values[i][0], values[i][2], values[i][3], values[i][1], values[i][4]));
		}
	}

	@Test
	public void testView() {

		assertEquals(getCandleHistory().size(), values.length);

		CandleHistoryView view = getCandleHistory().newView();
		assertEquals(view.size(), 0);

		view.updateView(getCandleHistory().getFirst(), getCandleHistory().getLast());

		assertEquals(view.size(), getCandleHistory().size());
		assertEquals(view.getFirst(), getCandleHistory().getFirst());
		assertEquals(view.getLast(), getCandleHistory().getLast());

		testIteration(view, getCandleHistory(), 0, 0);

		view.updateView(getCandleHistory().get(2), getCandleHistory().getLast());

		assertEquals(view.size(), getCandleHistory().size() - 2);
		assertEquals(view.getFirst(), getCandleHistory().get(2));
		assertEquals(view.getLast(), getCandleHistory().getLast());

		testIteration(view, getCandleHistory(), 2, 0);

		view.updateView(getCandleHistory().get(1), getCandleHistory().get(getCandleHistory().size() - 2));

		assertEquals(view.size(), getCandleHistory().size() - 2);
		assertEquals(view.getFirst(), getCandleHistory().get(1));
		assertEquals(view.getLast(), getCandleHistory().get(getCandleHistory().size() - 2));

		testIteration(view, getCandleHistory(), 1, 1);
	}

	private void testIteration(CandleHistoryView view, CandleHistory history, int ignoreAtStart, int ignoreAtEnd){
		Iterator<Candle> v = view.iterator();
		Iterator<Candle> h = history.iterator();

		while(ignoreAtStart-- > 0){
			h.next();
		}

		while(v.hasNext()) {
			assertEquals(v.next(), h.next());
		}

		while(ignoreAtEnd-- > 0){
			h.next();
		}

		assertFalse(h.hasNext());
	}
}