package com.univocity.trader.notification;

import com.univocity.trader.account.*;

public interface OrderEventListener {

	OrderEventListener NOOP = (order, trader, client) -> {
	};

	void onOrderUpdate(Order order, Trader trader, Client client);

}
