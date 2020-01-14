package com.univocity.trader.exchange.interactivebrokers.model.account;

import com.ib.client.*;

class PortfolioEntry {
	public final Contract contract;
	public final double position;
	public final double marketPrice;
	public final double marketValue;
	public final double averageCost;
	public final double unrealizedPNL;
	public final double realizedPNL;
	public final String accountName;

	PortfolioEntry(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		this.contract = contract;
		this.position = position;
		this.marketPrice = marketPrice;
		this.marketValue = marketValue;
		this.averageCost = averageCost;
		this.unrealizedPNL = unrealizedPNL;
		this.realizedPNL = realizedPNL;
		this.accountName = accountName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return this.contract.equals(((PortfolioEntry) o).contract);
	}

	@Override
	public int hashCode() {
		return contract.hashCode();
	}
}
