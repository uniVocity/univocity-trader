package com.univocity.trader.account;

import com.univocity.trader.*;

import java.util.*;

public final class OrderBook {

	private final String symbol;
	private final int depth;
	private final TreeMap<Double, Double> bids = new TreeMap<>(Comparator.reverseOrder());
	private final TreeMap<Double, Double> asks = new TreeMap<>(Comparator.naturalOrder());
	private ClientAccount account;

	public OrderBook(ClientAccount account, String symbol, int depth) {
		this.symbol = symbol;
		this.depth = depth;
		this.account = account;
	}

	public void addBid(double price, double quantity) {
		add(price, quantity, bids);
	}

	public void addAsk(double price, double quantity) {
		add(price, quantity, asks);
	}

	private void add(double price, double quantity, TreeMap<Double, Double> map) {
		map.put(price, quantity);
		while (map.size() > depth) {
			map.remove(map.lastKey());
		}
	}

	public double getAverageAskAmount(int depth) {
		return getAverageAmount(asks, depth);
	}

	public double getAverageBidAmount(int depth) {
		return getAverageAmount(bids, depth);
	}

	private double getAverageAmount(Map<Double, Double> m, int depth) {
		double pricesTimesQuantities = 0.0;
		double totalQuantity = 0.0;

		for (Map.Entry<Double, Double> e : m.entrySet()) {
			pricesTimesQuantities += e.getKey() * e.getValue();
			totalQuantity += e.getValue();

			if (--depth <= 0) {
				break;
			}
		}

		if (totalQuantity == 0.0) {
			return 0.0;
		}
		return pricesTimesQuantities / totalQuantity;
	}

	private double estimateFillPrice(Map<Double, Double> m, double quantityToFill) {
		double pricesTimesQuantities = 0.0;
		double totalQuantity = 0.0;

		for (Map.Entry<Double, Double> e : m.entrySet()) {
			double maxQuantity = e.getValue();
			double quantity = quantityToFill;
			quantityToFill -= maxQuantity;

			if (quantityToFill > 0) {
				quantity = maxQuantity;
			}

			pricesTimesQuantities += e.getKey() * quantity;
			totalQuantity += quantity;

			if (quantityToFill <= 0) {
				break;
			}
		}

		if (totalQuantity <= 0.0) {
			return 0.0;
		}
		return pricesTimesQuantities / totalQuantity;
	}

	public double getSpread(int depth) {
		double ask = getAverageAskAmount(depth);
		double bid = getAverageBidAmount(depth);

		return ask - bid;
	}

	public double getAverageAskAmount(double quantityToFill) {
		return estimateFillPrice(asks, quantityToFill);
	}

	public double getAverageBidAmount(double quantityToFill) {
		return estimateFillPrice(bids, quantityToFill);
	}

	public double getSpread(double quantityToFill) {
		double ask = getAverageAskAmount(quantityToFill);
		double bid = getAverageBidAmount(quantityToFill);

		return ask - bid;
	}

	public String getSymbol() {
		return symbol;
	}

	public int getDepth() {
		return depth;
	}

	public Map<Double, Double> getBids() {
		return bids;
	}

	public Map<Double, Double> getAsks() {
		return asks;
	}

	public OrderBook update(int depth) {
		return account.getOrderBook(this.symbol, depth);
	}
}
