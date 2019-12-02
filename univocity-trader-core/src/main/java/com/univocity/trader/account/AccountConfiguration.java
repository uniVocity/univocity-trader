package com.univocity.trader.account;

/**
 * Basic configuration on an account. Allows to define maximum a minimum investment amounts to one or more symbols,
 * as well as assigning {@link OrderManager}s to different symbols.
 */
public interface AccountConfiguration {

	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	double getAmount(String symbol);

	/**
	 * Assigns a maximum investment percentage, relative to the whole account balance, to one or more symbols. E.g. if the account balance is $1000.00
	 * and the percentage is set to 20.0, the account will never buy more than $200.00 worth of an asset and the remaining $800 will be used for
	 * other symbols.
	 *
	 * @param percentage the maximum percentage (from 0.0 to 100.0) of the account balance that can be allocated to any of the given symbols.
	 * @param symbols    the specific symbols to which the percentage applies. If none given then the percentage will be applied to all
	 *                   symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration maximumInvestmentPercentagePerAsset(double percentage, String... symbols);

	/**
	 * Assigns a maximum investment amount, relative to the whole account balance, to one or more symbols. E.g. if the account balance is $1000.00
	 * and the amount is set to $400.00, the account will never buy more than $400.00 of an asset. The remaining $600 of the balance will be used
	 * for other symbols.
	 *
	 * @param maximumAmount the maximum amount to be spent in any of the given symbols.
	 * @param symbols       the specific symbols to which the limit applies. If none then the limit will be applied to all
	 *                      symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration maximumInvestmentAmountPerAsset(double maximumAmount, String... symbols);

	/**
	 * Assigns a maximum percentage of funds to be used in a single trade. The percentage is relative to the whole account balance and can be applied
	 * to one or more symbols. E.g.  if the account balance is $1000.00 and the percentage is set to 5.0, the account will never buy more than $50.00
	 * at once. If another buy signal is received to buy into the same asset, that next purchase will be limited to the maximum 5% of the account
	 * balance as well, and so on.
	 *
	 * @param percentage the maximum percentage (from 0.0 to 100.0) of the account balance that can be allocated to any of the given symbols.
	 * @param symbols    the specific symbols to which the percentage applies. If none given then the percentage will be applied to all
	 *                   symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration maximumInvestmentPercentagePerTrade(double percentage, String... symbols);

	/**
	 * Assigns a maximum amount of funds to be used in a single trade, which can be applied to one or more symbols.
	 * E.g.  if the account balance is $1000.00 and the maximum amount per trade is set to $100.0, the account will never buy more than $100.00
	 * at once. If another buy signal is received to buy into the same asset, that next purchase will be limited to the maximum $100.00, and so on.
	 *
	 * @param maximumAmount the maximum amount that can be allocated to any single trade for the given symbols.
	 * @param symbols       the specific symbols to which the limit applies. If none given then the limit will be applied to all
	 *                      symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration maximumInvestmentAmountPerTrade(double maximumAmount, String... symbols);

	/**
	 * Assigns a minimum amount of funds to be used in a single trade, which can be applied to one or more symbols.
	 * E.g. if the minimum amount is set to $10.00, then the {@link Trader} will never open a buy order that is worh less than $10.00.
	 *
	 * @param minimumAmount the minimum amount to invest in any trade for the given symbols.
	 * @param symbols       the specific symbols to which the minimum applies. If none given then the minimum will be applied to all
	 *                      symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration minimumInvestmentAmountPerTrade(double minimumAmount, String... symbols);

	/**
	 * Assigns an {@link OrderManager} for the given symbols. By default, the {@link DefaultOrderManager} will be used when trading all symbols of
	 * this account. Use this method to replace with your own {@link OrderManager} implementation.
	 *
	 * @param orderManager the order manager to be used to control the lifecycle of trades made for the given symbols
	 * @param symbols      the specific symbols which should use the given order manager. If none given then the order manager be used to manage all
	 *                     orders made for the symbols traded by this account.
	 *
	 * @return this configuration object, for further settings.
	 */
	AccountConfiguration setOrderManager(OrderManager orderManager, String... symbols);
}
