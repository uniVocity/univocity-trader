package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

public class AccountManager implements ClientAccount {
	private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
	private static final AtomicLong NULL = new AtomicLong(0);
	private final AtomicLong tradeIdGenerator = new AtomicLong(0);

	final Map<String, AtomicLong> balanceUpdateCounts = new ConcurrentHashMap<>();

	final AccountConfiguration<?> configuration;

	private static final long BALANCE_EXPIRATION_TIME = minutes(10).ms;
	private static final long FREQUENT_BALANCE_UPDATE_INTERVAL = seconds(15).ms;

	private long lastBalanceSync = 0L;
	final ConcurrentHashMap<String, Balance> balances = new ConcurrentHashMap<>();
	Balance[] balancesArray;
	private final Lock balanceLock;

	final Client client;
	private final ClientAccount account;
	final double marginReserveFactor;
	final double marginReserveFactorPct;
	private final int accountHash;

	final Map<String, double[]> latestPrices = new HashMap<>();
	private static final double[] DEFAULT = new double[]{-1.0};
	Map<String, TradingManager[]> tradingManagers;
	final Supplier<SignalRepository> signalRepository;

	public AccountManager(ClientAccount account, AccountConfiguration<?> configuration, Supplier<SignalRepository> signalRepository) {
		if (StringUtils.isBlank(configuration.referenceCurrency())) {
			throw new IllegalConfigurationException("Please configure the reference currency symbol for the account");
		}
		if (configuration.getAllSymbolPairs().isEmpty()) {
			throw new IllegalConfigurationException("Please configure traded symbol pairs");
		}
		this.accountHash = configuration.id().hashCode();
		this.account = account;
		this.configuration = configuration;

		this.marginReserveFactor = account.marginReservePercentage() / 100.0;
		this.marginReserveFactorPct = marginReserveFactor;

		this.client = new Client(this);

		this.balanceLock = account.isSimulated() ? new FakeLock() : new ReentrantLock();

		if (account.marginReservePercentage() < 100) {
			throw new IllegalStateException("Margin reserve percentage must be at least 100%");
		}
		this.signalRepository = signalRepository;
	}

	public Client getClient() {
		return client;
	}

	ConcurrentHashMap<String, Balance> getBalances() {
		updateBalances(false);
		return balances;
	}


	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	public double getAmount(String symbol) {
		return getBalance(symbol, Balance::getFree);
	}

	public double getShortedAmount(String symbol) {
		return getBalance(symbol, Balance::getShorted);
	}

	Balance getBalance(String symbol) {
		Balance out = balances.get(symbol);
		if (out == null) {
			out = new Balance(this, symbol);
			balances.put(symbol, out);
			balancesArray = null;
		}
		return out;
	}

	public final void consumeBalance(String symbol, Consumer<Balance> consumer) {
		modifyBalance(symbol, consumer);
	}

	public final void modifyBalance(String symbol, Consumer<Balance> consumer) {
		balanceLock.lock();
		try {
			consumer.accept(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	public final <T> T queryBalance(String symbol, Function<Balance, T> function) {
		balanceLock.lock();
		try {
			return function.apply(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	public final double getBalance(String symbol, ToDoubleFunction<Balance> function) {
		balanceLock.lock();
		try {
			return function.applyAsDouble(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	boolean lockTrading(String assetSymbol) {
		return queryBalance(assetSymbol, b -> {
			if (b.isTradingLocked()) {
				return false;
			}
			b.lockTrading();
			return true;
		});
	}

	void unlockTrading(String assetSymbol) {
		modifyBalance(assetSymbol, Balance::unlockTrading);
	}

	private int hash(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
		int result = 31 + accountHash;
		result = 31 * result + assetSymbol.hashCode();
		result = 31 * result + fundSymbol.hashCode();
		result = 31 * result + tradeSide.hashCode();
		return result;
	}

	final double allocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide, TradingManager tradingManager) {
		long a = balanceUpdateCounts.getOrDefault(assetSymbol, NULL).get();
		long f = balanceUpdateCounts.getOrDefault(fundSymbol, NULL).get();
		Integer hash = hash(assetSymbol, fundSymbol, tradeSide);
		long[] cached = tradingManager.fundAllocationCache.get(hash);
		if (cached == null) {
			double funds = tradingManager.executeAllocateFunds(assetSymbol, fundSymbol, tradeSide);
			if (a > 0 && f > 0) {
				cached = new long[3];
				cached[0] = a;
				cached[1] = f;
				cached[2] = Double.doubleToLongBits(funds);
				tradingManager.fundAllocationCache.put(hash, cached);
			}
			return funds;
		} else if (cached[0] != a || cached[1] != f) {
			cached[0] = a;
			cached[1] = f;
			double funds = tradingManager.executeAllocateFunds(assetSymbol, fundSymbol, tradeSide);
			cached[2] = Double.doubleToLongBits(funds);
			tradingManager.fundAllocationCache.put(hash, cached);
			return funds;
		} else {
			return Double.longBitsToDouble(cached[2]);
		}
	}


	public double getTotalFundsInReferenceCurrency() {
		return getTotalFundsIn(configuration.referenceCurrency());
	}

	public double getTotalFundsIn(String currency) {
		final Balance[] tmp;
		balanceLock.lock();
		double total = 0.0;
		try {
			if (balancesArray == null) {
				tmp = balancesArray = balances.values().toArray(new Balance[0]);
			} else {
				tmp = balancesArray;
			}
			for (int i = 0; i < tmp.length; i++) {
				Balance b = tmp[i];
				String symbol = b.getSymbol();
				double quantity = b.getTotal();

				String[] shortedAssetSymbols = b.getShortedAssetSymbols();
				for (int j = 0; j < shortedAssetSymbols.length; j++) {
					String shorted = shortedAssetSymbols[j];
					double reserve = b.getMarginReserve(shorted);
					double marginWithoutReserve = reserve / marginReserveFactorPct;
					double accountBalanceForMargin = reserve - marginWithoutReserve;
					double shortedQuantity = getBalance(shorted, Balance::getShorted);
					double originalShortedPrice = marginWithoutReserve / shortedQuantity;
					double totalInvestmentOnShort = shortedQuantity * originalShortedPrice;
					double totalAtCurrentPrice = multiplyWithLatestPrice(shortedQuantity, shorted, symbol);
					double shortProfitLoss = totalInvestmentOnShort - totalAtCurrentPrice;

					total += accountBalanceForMargin + shortProfitLoss;
				}

				if (currency.equals(symbol)) {
					total += quantity;
				} else {
					total += multiplyWithLatestPrice(quantity, symbol, currency);
				}
			}
		} finally {
			balanceLock.unlock();
		}
		return total;
	}

	public double getLatestPrice(String symbol) {
		return latestPrices.getOrDefault(symbol, DEFAULT)[0];
	}

	private double multiplyWithLatestPrice(double quantity, String symbol, String currency) {
		double price = getLatestPrice(symbol + currency);
		if (price > 0.0) {
			return quantity * price;
		} else {
			price = getLatestPrice(currency + symbol);
			if (price > 0.0) {
				return quantity / price;
			}
		}
		return 0.0;
	}


	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		Map<String, Balance> positions = account.updateBalances(false);
		positions.entrySet().stream()
				.filter((e) -> e.getValue().getTotal() > 0.00001)
				.forEach((e) -> out
						.append(e.getKey())
						.append(" = $")
						.append(SymbolPriceDetails.toBigDecimal(2, e.getValue().getTotal()).toPlainString())
						.append('\n'));

		return out.toString();
	}

	void executeUpdateBalances() {
		if (balanceLock.tryLock()) {
			try {
				Map<String, Balance> updatedBalances = account.updateBalances(true);
				if (updatedBalances != null && updatedBalances != balances && !updatedBalances.isEmpty()) {
					log.trace("Balances updated - available: " + new TreeMap<>(updatedBalances).values());
					updatedBalances.keySet().retainAll(configuration.symbols());

					this.balances.clear();
					this.balances.putAll(updatedBalances);
					this.balancesArray = null;


					updatedBalances.values().removeIf(b -> b.getTotal() == 0);
					log.debug("Balances updated - trading: " + new TreeMap<>(updatedBalances).values());
				}
				lastBalanceSync = System.currentTimeMillis();
			} finally {
				balanceLock.unlock();
			}
		}
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		if (!force && (System.currentTimeMillis() - lastBalanceSync < FREQUENT_BALANCE_UPDATE_INTERVAL)) {
			return balances;
		}
		executeUpdateBalances();

		return balances;
	}

	public String getReferenceCurrencySymbol() {
		return configuration.referenceCurrency();
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		if (orderDetails != null) {
			if (orderDetails.isCancelled()) {
				return null;
			}
			if (orderDetails.getQuantity() == 0) {
				throw new IllegalArgumentException("No quantity specified for order " + orderDetails);
			}
			if (orderDetails.getPrice() == 0 && orderDetails.getType() == Order.Type.LIMIT) {
				throw new IllegalArgumentException("No price specified for LIMIT order " + orderDetails);
			}

			Order order = account.executeOrder(orderDetails);
			if (order != null && order.getStatus() == CANCELLED) {
				TradingManager.logOrderStatus("Could not create order. ", order);
				return null;
			} else {
				return order;
			}
		}
		return null;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	public TradingFees getTradingFees() {
		return ClientAccount.super.getTradingFees();
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return account.updateOrderStatus(order);
	}

	@Override
	public void cancel(Order order) {
		account.cancel(order);
	}


	@Override
	public final boolean isSimulated() {
		return account.isSimulated();
	}

	public String accountId() {
		return configuration.id();
	}

	public boolean canShortSell() {
		return configuration.shortingEnabled();
	}

	public final double marginReserveFactorPct() {
		return marginReserveFactorPct;
	}

	public AtomicLong getTradeIdGenerator() {
		return tradeIdGenerator;
	}

	public Map<String, Balance> getBalanceSnapshot() {
		balanceLock.lock();
		try {
			return balances.entrySet()
					.stream()
					.collect(Collectors
							.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
		} finally {
			balanceLock.unlock();
		}
	}

	public String email() {
		return configuration.email();
	}

	public TimeZone timeZone() {
		return configuration.timeZone();
	}

	public Map<String, String[]> getAllSymbolPairs() {
		return configuration.getAllSymbolPairs();
	}

	public void createTradingManager(String symbol, Exchange exchange, OrderExecutionToEmail emailNotifier, Parameters parameters) {
		if (this.tradingManagers == null) {
			this.tradingManagers = new HashMap<>();
		}
		Map<String, List<TradingManager>> tmp = new HashMap<>();
		Set<Object> allInstances = new HashSet<>();
		initialize(symbol, tmp, configuration, exchange, emailNotifier, parameters, allInstances);
		configuration.tradingGroups().forEach(g -> initialize(symbol, tmp, g, exchange, emailNotifier, parameters, allInstances));
		allInstances.clear();

		if (tmp.isEmpty()) {
			throw new IllegalStateException("Account has not been configured to trade.");
		}

		tmp.forEach((k, v) -> tradingManagers.put(k, v.toArray(TradingManager[]::new)));
	}

	private void initialize(String symbol, Map<String, List<TradingManager>> out, AbstractTradingGroup<?> group, Exchange exchange, OrderExecutionToEmail emailNotifier, Parameters parameters, Set<Object> allInstances) {
		if (!group.isConfigured()) {
			return;
		}
		if (emailNotifier != null) {
			group.listeners().add(emailNotifier);
		}

		SymbolPriceDetails priceDetails = new SymbolPriceDetails(exchange, getReferenceCurrencySymbol());

		String[] pair = group.symbolPairs().get(symbol);
		if (pair != null) {
			String assetSymbol = pair[0].intern();
			String fundSymbol = pair[1].intern();

			latestPrices.put(symbol, new double[1]);

			TradingManager tradingManager = new TradingManager(group, exchange, priceDetails, this, assetSymbol, fundSymbol, parameters, allInstances);
			out.computeIfAbsent(symbol, s -> new ArrayList<>()).add(tradingManager);
		}
	}

	public Map<String, double[]> getLatestPrices() {
		return latestPrices;
	}

	public TradingManager[] getTradingManagersOf(String symbol) {
		if (tradingManagers == null || tradingManagers.isEmpty()) {
			throw new IllegalStateException("No trading managers created for this account");
		}
		return tradingManagers.get(symbol);
	}

	public <T> T getFromFirstTradingManager(Function<TradingManager, T> f) {
		if (tradingManagers == null || tradingManagers.isEmpty()) {
			throw new IllegalStateException("No trading managers created for this account");
		}

		for (Map.Entry<String, TradingManager[]> entry : tradingManagers.entrySet()) {
			TradingManager[] v = entry.getValue();
			for (int i = 0; i < v.length; i++) {
				T out = f.apply(v[i]);
				if (out != null) {
					return out;
				}
			}
		}
		return null;
	}

	public void forEachTradingManager(Consumer<TradingManager> consumer) {
		if (tradingManagers == null) {
			throw new IllegalStateException("No trading managers created for this account");
		}
		tradingManagers.forEach((k, v) -> {
			for (int i = 0; i < v.length; i++) {
				consumer.accept(v[i]);
			}
		});
	}

	boolean hasOtherOpenTrades(TradingManager tradingManager) {
		TradingManager[] tradingManagers = this.tradingManagers.get(tradingManager.symbol);
		for (int i = 0; i < tradingManagers.length; i++) {
			if (tradingManagers[i] != tradingManager) {
				if (tradingManagers[i].trader.hasOpenTrades()) {
					return true;
				}
			}
		}
		return false;
	}
}
