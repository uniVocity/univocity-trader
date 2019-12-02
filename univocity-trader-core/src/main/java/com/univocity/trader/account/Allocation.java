package com.univocity.trader.account;

public class Allocation {

	public static Allocation NO_LIMITS = new Allocation();

	private double maximumAmountPerAsset = Integer.MAX_VALUE;
	private double maximumPercentagePerAsset = 100.0;

	private Allocation() {

	}

	static Allocation maximumAmountPerAsset(double amount) {
		Allocation out = new Allocation();
		out.maximumAmountPerAsset = amount;
		out.maximumPercentagePerAsset = -1.0;
		return out;
	}

	static Allocation maximumPercentagePerAsset(double percentage) {
		Allocation out = new Allocation();
		out.maximumAmountPerAsset = -1.0;
		out.maximumPercentagePerAsset = percentage;
		return out;
	}

	public double getMaximumAmountPerAsset() {
		return maximumAmountPerAsset;
	}

	public Allocation setMaximumAmountPerAsset(double maximumAmountPerAsset) {
		this.maximumAmountPerAsset = maximumAmountPerAsset;
		return this;
	}

	public double getMaximumPercentagePerAsset() {
		return maximumPercentagePerAsset;
	}

	public Allocation setMaximumPercentagePerAsset(double maximumPercentagePerAsset) {
		this.maximumPercentagePerAsset = maximumPercentagePerAsset;
		return this;
	}
}
