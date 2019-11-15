package com.univocity.trader.utils;

import org.junit.*;

import static junit.framework.TestCase.*;

public class CircularListTest {

	@Test
	public void testCircularity(){
		CircularList l = new CircularList(3);

		l.update(0.5);
		assertEquals(1, l.size());
		assertEquals(0.5, l.getRecentValue(1), 0.1);
		assertEquals(0, l.getRecentValue(2), 0.1);
		assertEquals(0, l.getRecentValue(3), 0.1);
		assertEquals(0.5, l.first(), 0.1);
		assertEquals(0.5, l.last(), 0.1);

		l.add(1);
		assertEquals(1, l.size());
		assertEquals(1, l.getRecentValue(1), 0.1);
		assertEquals(0, l.getRecentValue(2), 0.1);
		assertEquals(0, l.getRecentValue(3), 0.1);
		assertEquals(1, l.first(), 0.1);
		assertEquals(1, l.last(), 0.1);

		l.update(1.5);
		assertEquals(2, l.size());
		assertEquals(1.5, l.getRecentValue(1), 0.1);
		assertEquals(1, l.getRecentValue(2), 0.1);
		assertEquals(0, l.getRecentValue(3), 0.1);
		assertEquals(1, l.first(), 0.1);
		assertEquals(1.5, l.last(), 0.1);

		l.add(2);
		assertEquals(2, l.size());
		assertEquals(1, l.getRecentValue(2), 0.1);
		assertEquals(2, l.getRecentValue(1), 0.1);
		assertEquals(1, l.first(), 0.1);
		assertEquals(2, l.last(), 0.1);

		l.update(2.5);
		assertEquals(3, l.size());
		assertEquals(2.5, l.getRecentValue(1), 0.1);
		assertEquals(2, l.getRecentValue(2), 0.1);
		assertEquals(1, l.getRecentValue(3), 0.1);
		assertEquals(1, l.first(), 0.1);
		assertEquals(2.5, l.last(), 0.1);

		l.add(3);
		assertEquals(3, l.size());
		assertEquals(1, l.getRecentValue(3), 0.1);
		assertEquals(2, l.getRecentValue(2), 0.1);
		assertEquals(3, l.getRecentValue(1), 0.1);
		assertEquals(1, l.first(), 0.1);
		assertEquals(3, l.last(), 0.1);

		l.update(3.5);
		assertEquals(3, l.size());
		assertEquals(3.5, l.getRecentValue(1), 0.1);
		assertEquals(3, l.getRecentValue(2), 0.1);
		assertEquals(2, l.getRecentValue(3), 0.1);
		assertEquals(2, l.first(), 0.1);
		assertEquals(3.5, l.last(), 0.1);

		l.add(4);
		assertEquals(3, l.size());
		assertEquals(2, l.getRecentValue(3), 0.1);
		assertEquals(3, l.getRecentValue(2), 0.1);
		assertEquals(4, l.getRecentValue(1), 0.1);
		assertEquals(2, l.first(), 0.1);
		assertEquals(4, l.last(), 0.1);

		l.update(4.5);
		assertEquals(3, l.size());
		assertEquals(4.5, l.getRecentValue(1), 0.1);
		assertEquals(4, l.getRecentValue(2), 0.1);
		assertEquals(3, l.getRecentValue(3), 0.1);
		assertEquals(3, l.first(), 0.1);
		assertEquals(4.5, l.last(), 0.1);

		l.add(5);
		assertEquals(3, l.size());
		assertEquals(3, l.getRecentValue(3), 0.1);
		assertEquals(4, l.getRecentValue(2), 0.1);
		assertEquals(5, l.getRecentValue(1), 0.1);
		assertEquals(3, l.first(), 0.1);
		assertEquals(5, l.last(), 0.1);

		l.update(5.5);
		assertEquals(3, l.size());
		assertEquals(5.5, l.getRecentValue(1), 0.1);
		assertEquals(5, l.getRecentValue(2), 0.1);
		assertEquals(4, l.getRecentValue(3), 0.1);
		assertEquals(4, l.first(), 0.1);
		assertEquals(5.5, l.last(), 0.1);

		l.add(6);
		assertEquals(3, l.size());
		assertEquals(4, l.getRecentValue(3), 0.1);
		assertEquals(5, l.getRecentValue(2), 0.1);
		assertEquals(6, l.getRecentValue(1), 0.1);
		assertEquals(4, l.first(), 0.1);
		assertEquals(6, l.last(), 0.1);

		l.update(6.5);
		assertEquals(3, l.size());
		assertEquals(6.5, l.getRecentValue(1), 0.1);
		assertEquals(6, l.getRecentValue(2), 0.1);
		assertEquals(5, l.getRecentValue(3), 0.1);
		assertEquals(5, l.first(), 0.1);
		assertEquals(6.5, l.last(), 0.1);

		l.add(7);
		assertEquals(3, l.size());
		assertEquals(5, l.getRecentValue(3), 0.1);
		assertEquals(6, l.getRecentValue(2), 0.1);
		assertEquals(7, l.getRecentValue(1), 0.1);
		assertEquals(5, l.first(), 0.1);
		assertEquals(7, l.last(), 0.1);

		l.update(7.5);
		assertEquals(3, l.size());
		assertEquals(6, l.getRecentValue(3), 0.1);
		assertEquals(7, l.getRecentValue(2), 0.1);
		assertEquals(7.5, l.getRecentValue(1), 0.1);

		assertEquals(6, l.first(), 0.1);
		assertEquals(7.5, l.last(), 0.1);

		l.add(8);
		assertEquals(3, l.size());
		assertEquals(6, l.getRecentValue(3), 0.1);
		assertEquals(7, l.getRecentValue(2), 0.1);
		assertEquals(8, l.getRecentValue(1), 0.1);
		assertEquals(6, l.first(), 0.1);
		assertEquals(8, l.last(), 0.1);

		l.add(9);
		assertEquals(3, l.size());
		assertEquals(7, l.getRecentValue(3), 0.1);
		assertEquals(8, l.getRecentValue(2), 0.1);
		assertEquals(9, l.getRecentValue(1), 0.1);
		assertEquals(7, l.first(), 0.1);
		assertEquals(9, l.last(), 0.1);

		l.add(10);
		assertEquals(3, l.size());
		assertEquals(8, l.getRecentValue(3), 0.1);
		assertEquals(9, l.getRecentValue(2), 0.1);
		assertEquals(10, l.getRecentValue(1), 0.1);
		assertEquals(8, l.first(), 0.1);
		assertEquals(10, l.last(), 0.1);

		assertEquals(10, l.get(0), 0.1);
		assertEquals(8, l.get(1), 0.1);
		assertEquals(9, l.get(2), 0.1);
	}
}