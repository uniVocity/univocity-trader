package com.univocity.trader.config;

public class Allocation implements Cloneable {

	public static final double EFFECTIVELY_ZERO = 0.000000001;

	static Allocation NO_LIMITS = new Allocation();

	private double maximumAmountPerAsset = Integer.MAX_VALUE;
	private double maximumPercentagePerAsset = 100.0;

	private double maximumAmountPerTrade = Integer.MAX_VALUE;
	private double maximumPercentagePerTrade = 100.0;

	private double minimumAmountPerTrade = EFFECTIVELY_ZERO;

	Allocation() {

	}

	public double getMaximumAmountPerAsset() {
		return maximumAmountPerAsset;
	}

	public Allocation setMaximumAmountPerAsset(double maximumAmountPerAsset) {
		this.maximumAmountPerAsset = adjustAmount(maximumAmountPerAsset);
		return this;
	}

	public double getMaximumPercentagePerAsset() {
		return maximumPercentagePerAsset;
	}

	public Allocation setMaximumPercentagePerAsset(double maximumPercentagePerAsset) {
		this.maximumPercentagePerAsset = adjustPercentage(maximumPercentagePerAsset);
		return this;
	}

	public double getMaximumAmountPerTrade() {
		return maximumAmountPerTrade;
	}

	public Allocation setMaximumAmountPerTrade(double maximumAmountPerTrade) {
		this.maximumAmountPerTrade = adjustAmount(maximumAmountPerTrade);
		return this;
	}

	public double getMaximumPercentagePerTrade() {
		return maximumPercentagePerTrade;
	}

	public Allocation setMaximumPercentagePerTrade(double maximumPercentagePerTrade) {
		this.maximumPercentagePerTrade = adjustPercentage(maximumPercentagePerTrade);
		return this;
	}

	public double getMinimumAmountPerTrade() {
		return minimumAmountPerTrade;
	}

	public Allocation setMinimumAmountPerTrade(double minimumAmountPerTrade) {
		this.minimumAmountPerTrade = adjustAmount(minimumAmountPerTrade);
		return this;
	}

	private static double adjustPercentage(double percentage) {
		percentage = Math.max(percentage, 0.0);
		percentage = Math.min(percentage, 100.0);
		return percentage;
	}

	private static double adjustAmount(double maximumAmount) {
		return Math.max(maximumAmount, 0.0);
	}

	@Override
	public Allocation clone() {
		try {
			return (Allocation) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
}
