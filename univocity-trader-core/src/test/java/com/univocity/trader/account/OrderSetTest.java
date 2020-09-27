package com.univocity.trader.account;

import org.apache.commons.lang3.*;
import org.junit.*;

import static org.junit.Assert.*;

public class OrderSetTest {

	@Test
	public void testModification() {
		OrderSet p = new OrderSet();
		for (long i = 0; i < 50; i++) {
			p.addOrReplace(newOrder(i));
			assertEquals(i + 1, p.size());
			assertTrue(p.contains(newOrder(i)));
		}
		assertEquals(50, p.size());
		assertFalse(p.contains(newOrder(50)));

		for (long i = 49; i >= 0; i--) {
			assertEquals(i + 1, p.size());
			p.remove(newOrder(i));
			assertEquals(i, p.size());
			assertFalse(p.contains(newOrder(i)));
		}

		assertEquals(p.size(), 0);
	}

	@Test
	public void testChangesInMiddle() {
		OrderSet p = new OrderSet();
		for (long i = 0; i < 10; i++) {
			p.addOrReplace(newOrder(i * 2));
			assertEquals(i + 1, p.size());
			assertTrue(p.contains(newOrder(i * 2)));
		}
		assertEquals(10, p.size());

		assertFalse(p.contains(newOrder(5)));
		p.addOrReplace(newOrder(5));
		assertTrue(p.contains(newOrder(5)));

		assertTrue(p.contains(newOrder(6)));
		p.remove(newOrder(6));
		assertFalse(p.contains(newOrder(6)));

		assertTrue(p.contains(newOrder(0)));
		p.remove(newOrder(0));
		assertFalse(p.contains(newOrder(0)));

		assertEquals(9, p.size());

		p.clear();
		assertEquals(0, p.size());
	}

	@Test
	public void testAddAll() {
		OrderSet s1 = new OrderSet();
		for (long i = 0; i < 10; i++) {
			s1.addOrReplace(newOrder(i));
		}

		OrderSet s2 = new OrderSet();
		for (long i = 10; i < 20; i++) {
			s2.addOrReplace(newOrder(i));
		}

		OrderSet s3 = new OrderSet();
		s3.addAll(s1);
		s3.addAll(s2);

		for (long i = 0; i < 20; i++) {
			assertTrue(s3.contains(newOrder(i)));
		}
		assertEquals(20, s3.size());
	}

	@Test
	public void testNullRemoval() {
		OrderSet s1 = new OrderSet();
		for (long i = 0; i < 10; i++) {
			s1.addOrReplace(newOrder(i));
		}

		for (int i = 0; i < 5; i++) {
			s1.elements[i * 2] = null;
		}
		assertEquals(10, s1.size());
		s1.removeNulls();
		assertEquals(5, s1.size());

		assertTrue(s1.contains(newOrder(0)));
		assertTrue(s1.contains(newOrder(2)));
		assertTrue(s1.contains(newOrder(4)));
		assertTrue(s1.contains(newOrder(6)));
		assertTrue(s1.contains(newOrder(8)));

		for (int i = 0; i < 5; i++) {
			s1.elements[i] = null;
		}
		s1.removeNulls();
		assertTrue(s1.isEmpty());
	}

	private Order newOrder(long orderId) {
		Order out = new Order(orderId,"a", "b", Order.Side.BUY, Trade.Side.LONG, 0) {
			@Override
			public int compareTo(Order o) {
				return StringUtils.leftPad(this.getOrderId(), 2, '0').compareTo(StringUtils.leftPad(o.getOrderId(), 2, '0'));
			}
		};
		return out;
	}
}