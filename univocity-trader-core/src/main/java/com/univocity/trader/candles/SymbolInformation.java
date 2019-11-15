package com.univocity.trader.candles;

import java.math.*;

public class SymbolInformation {

	public static BigDecimal DEFAULT_MINIMUM_ASSETS_PER_ORDER = new BigDecimal(0.000001);// not zero to prevent trades that would use pennies.

	private final String symbol;
	private int priceDecimalPlaces = 2;
	private int quantityDecimalPlaces = 0;
	private BigDecimal minimumAssetsPerOrder = DEFAULT_MINIMUM_ASSETS_PER_ORDER;

	public SymbolInformation(String symbol) {
		this.symbol = symbol;
	}

	public String symbol() {
		return symbol;
	}

	public int priceDecimalPlaces() {
		return priceDecimalPlaces;
	}

	public SymbolInformation priceDecimalPlaces(int priceDecimalPlaces) {
		this.priceDecimalPlaces = priceDecimalPlaces;
		return this;
	}

	public int quantityDecimalPlaces() {
		return quantityDecimalPlaces;
	}

	public SymbolInformation quantityDecimalPlaces(int quantityDecimalPlaces) {
		this.quantityDecimalPlaces = quantityDecimalPlaces;
		return this;
	}

	public BigDecimal minimumAssetsPerOrder() {
		return minimumAssetsPerOrder;
	}

	public SymbolInformation minimumAssetsPerOrder(double minimumAssetsPerOrder) {
		return minimumAssetsPerOrder(new BigDecimal(minimumAssetsPerOrder));
	}

	public SymbolInformation minimumAssetsPerOrder(BigDecimal minimumAssetsPerOrder) {
		this.minimumAssetsPerOrder = minimumAssetsPerOrder;
		return this;
	}
}
