package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;

import static com.univocity.trader.account.Order.Side.*;

public class TradingManager {

	private static final Logger log = LoggerFactory.getLogger(TradingManager.class);

	private static final long FIFTEEN_SECONDS = TimeInterval.seconds(15).ms;

	private final String symbol;
	final String assetSymbol;
	final String fundSymbol;
	private final AccountManager tradingAccount;
	protected Trader trader;
	private Exchange<?> api;
	private final OrderEventListener[] notifications;
	protected Client client;
	private OrderExecutionToEmail emailNotifier;
	private final SymbolPriceDetails priceDetails;

	public TradingManager(Exchange api, SymbolPriceDetails priceDetails, AccountManager account, InstancesProvider<OrderEventListener> listenerProvider, String assetSymbol, String fundSymbol, Parameters params) {
		if (api == null) {
			throw new IllegalArgumentException("Exchange API implementation cannot be null");
		}
		if (priceDetails == null) {
			priceDetails = new SymbolPriceDetails(api);
		}
		if (account == null) {
			throw new IllegalArgumentException("Account manager cannot be null");
		}
		if (StringUtils.isBlank(assetSymbol)) {
			throw new IllegalArgumentException("Symbol of instrument to buy cannot be blank (examples: 'MSFT', 'BTC', 'EUR')");
		}
		if (StringUtils.isBlank(fundSymbol)) {
			throw new IllegalArgumentException("Currency cannot be blank (examples: 'USD', 'EUR', 'USDT', 'ETH')");
		}
		this.api = api;
		this.tradingAccount = account;
		this.assetSymbol = assetSymbol;
		this.fundSymbol = fundSymbol;
		this.symbol = assetSymbol + fundSymbol;
		this.priceDetails = priceDetails.switchToSymbol(symbol);

		this.notifications = listenerProvider != null ? listenerProvider.create(symbol, params) : new OrderEventListener[0];
		this.emailNotifier = getEmailNotifier();
		account.register(this);
	}

	public SymbolPriceDetails getPriceDetails() {
		return priceDetails;
	}

	public String getFundSymbol() {
		return fundSymbol;
	}

	public String getAssetSymbol() {
		return assetSymbol;
	}

	public double getLatestPrice() {
		return getLatestPrice(assetSymbol, fundSymbol);
	}

	public Candle getLatestCandle() {
		return trader.getCandle();
	}

	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		Candle lastCandle = getLatestCandle();
		if (lastCandle != null && (System.currentTimeMillis() - lastCandle.closeTime) < FIFTEEN_SECONDS) {
			return lastCandle.close;
		}
		return api.getLatestPrice(assetSymbol, fundSymbol);
	}

	public String getSymbol() {
		return symbol;
	}

	public Map<String, Double> getAllPrices() {
		return api.getLatestPrices();
	}

	public boolean hasAssets(Candle c) {
		double minimum = getPriceDetails().getMinimumOrderAmount(c.close);
		return getAssets() * c.close > minimum;
	}

	public final boolean buy(double quantity) {
		return processOrder(trader, tradingAccount.buy(assetSymbol, fundSymbol, quantity));
	}

	private boolean processOrder(Trader trader, Order order) {
		if (order != null) {
			trader.notifyTrade(trader.getCandle(), order);
			return true;
		}
		return false;
	}

//	public boolean switchTo(String ticker, Signal trade, String exitSymbol) {
//		String targetSymbol = exitSymbol + fundSymbol;
//		double targetUnitPrice = getLatestPrice(exitSymbol, fundSymbol);
//		if (targetUnitPrice <= 0.0) {
//			return false;
//		}
//
//		final Trader purchaseTrader = getTraderOf(targetSymbol);
//		if (trader != null) {
//			double quantityToSell = tradingAccount.allocateFunds(exitSymbol, assetSymbol);
//			double saleUnitPrice = trader.getLastClosingPrice();
//			double saleAmount = quantityToSell * saleUnitPrice;
//			double quantityToBuy = saleAmount / targetUnitPrice;
//
//			trader.setExitReason("Switching from " + ticker + " to " + targetSymbol);
//
//			if (trade == SELL) {
//				return processOrder(trader, tradingAccount.sell(assetSymbol, exitSymbol, quantityToSell));
//			} else {
//				return processOrder(purchaseTrader, tradingAccount.buy(exitSymbol, assetSymbol, quantityToBuy));
//			}
//		}
//		return false;
//	}

	public boolean sell(double quantity) {
		if (quantity <= 0.0) {
			return false;
		}
		return processOrder(trader, tradingAccount.sell(assetSymbol, fundSymbol, quantity));
	}

	public double getAssets() {
		return tradingAccount.getAmount(assetSymbol);
	}

	public double getCash() {
		return tradingAccount.getAmount(fundSymbol);
	}

	public double allocateFunds() {
		return tradingAccount.allocateFunds(assetSymbol);
	}

	public double getTotalFundsInReferenceCurrency() {
		return tradingAccount.getTotalFundsInReferenceCurrency();
	}

	public double getTotalFundsIn(String symbol) {
		return tradingAccount.getTotalFundsIn(symbol);
	}

	public boolean exitExistingPositions(String exitSymbol, Candle c) {
		boolean exited = false;
		for (TradingManager action : tradingAccount.getAllTradingManagers()) {
			if (action != this && action.hasAssets(c) && action.trader.switchTo(exitSymbol, c, action.symbol)) {
				exited = true;
				break;
			}
		}
		return exited;
	}

	public boolean waitingForBuyOrderToFill() {
		return tradingAccount.waitingForFill(assetSymbol, BUY);
	}

	public final Map<String, Balance> updateBalances() {
		return tradingAccount.updateBalances();

	}

	public String getReferenceCurrencySymbol() {
		return tradingAccount.getReferenceCurrencySymbol();
	}

	public Trader getTrader() {
		return this.trader;
	}

	public boolean isBuyLocked() {
		return tradingAccount.isBuyLocked(assetSymbol);
	}

	public AccountManager getAccount() {
		return tradingAccount;
	}

	public void cancelStaleOrders() {
		tradingAccount.cancelStaleOrders();
	}

	public TradingFees getTradingFees() {
		return tradingAccount.getTradingFees();
	}

	public void sendBalanceEmail(String title, Client client) {
		getEmailNotifier().sendBalanceEmail(title, client);
	}

	public OrderExecutionToEmail getEmailNotifier() {
		if (emailNotifier == null) {
			for (int i = 0; i < notifications.length; i++) {
				if (notifications[i] instanceof OrderExecutionToEmail) {
					emailNotifier = (OrderExecutionToEmail) notifications[i];
					break;
				}
			}
			if (emailNotifier == null) {
				emailNotifier = new OrderExecutionToEmail();
			}
			emailNotifier.initialize(this);
		}
		return emailNotifier;
	}

	void notifyTradeExecution(Order order) {
		for (int i = 0; i < notifications.length; i++) {
			try {
				notifications[i].onOrderUpdate(order, trader, client);
			} catch (Exception e) {
				log.error("Error executing update notification on order: " + order, e);
			}
		}
	}

//	boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol) {
//		return api.isDirectSwitchSupported(currentAssetSymbol, targetAssetSymbol);
//	}
}
