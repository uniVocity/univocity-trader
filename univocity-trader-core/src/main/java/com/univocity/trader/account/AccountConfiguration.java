package com.univocity.trader.account;

public interface AccountConfiguration {

	double getAmount(String symbol);

	AccountConfiguration maximumInvestmentPercentagePerAsset(double percentage, String... symbols);

	AccountConfiguration maximumInvestmentAmountPerAsset(double maximumAmount, String... symbols);

	AccountConfiguration setOrderManager(OrderManager orderCreator, String... symbols);
}
