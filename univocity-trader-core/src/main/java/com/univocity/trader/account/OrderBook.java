package com.univocity.trader.account;

import com.univocity.trader.*;

import java.util.*;

public class OrderBook {

	private final String symbol;
	private final int depth;
	private final Map<Double, Double> bids = new TreeMap<>(Comparator.reverseOrder());
	private final Map<Double, Double> asks = new TreeMap<>(Comparator.naturalOrder());
	private ClientAccount api;

	public OrderBook(ClientAccount api, String symbol, int depth) {
		this.symbol = symbol;
		this.depth = depth;
		this.api = api;
	}

	public void addBid(double price, double quantity){
		bids.put(price, quantity);
	}

	public void addAsk(double price, double quantity){
		asks.put(price, quantity);
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

			if(quantityToFill > 0){
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

	public OrderBook update(int depth){
		return api.getOrderBook(this.symbol, depth);
	}
}
