package com.univocity.trader.utils;

import com.univocity.trader.simulation.*;

@FunctionalInterface
public interface InstancesProvider<T> {

	T[] create(String symbol, Parameters params);
}
