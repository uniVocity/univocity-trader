package com.univocity.trader.account;

public interface AccountConfiguration {

	double getAmount(String symbol);

	AccountConfiguration maximumInvestmentPercentagePerAsset(double percentage, String... symbols);

	AccountConfiguration maximumInvestmentAmountPerAsset(double maximumAmount, String... symbols);

	AccountConfiguration maximumInvestmentPercentagePerTrade(double percentage, String... symbols);


	AccountConfiguration maximumInvestmentAmountPerTrade(double maximumAmount, String... symbols);


	AccountConfiguration minimumInvestmentAmountPerTrade(double minimumAmount, String... symbols);


	AccountConfiguration setOrderManager(OrderManager orderCreator, String... symbols);
}
