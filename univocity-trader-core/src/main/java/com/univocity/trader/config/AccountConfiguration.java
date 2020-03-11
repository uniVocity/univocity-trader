package com.univocity.trader.config;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.config.Allocation.*;
import static com.univocity.trader.config.Utils.*;

/**
 * Basic configuration on an account. Allows to define maximum a minimum investment amounts to one or more symbols,
 * as well as assigning {@link OrderManager}s to different symbols.
 */
public abstract class AccountConfiguration<T extends AccountConfiguration<T>> implements Cloneable {

	private static final OrderManager DEFAULT_ORDER_MANAGER = new DefaultOrderManager();
	private static final Set<String> supportedTimeZones = new TreeSet<>(List.of(TimeZone.getAvailableIDs()));
	private static final String supportedTimezoneDescription;

	static {
		StringBuilder tmp = new StringBuilder(3000);
		tmp.append(" Supported timezones:");
		for (String tz : supportedTimeZones) {
			tmp.append('\n').append(tz);
		}
		supportedTimezoneDescription = tmp.toString();
	}

	private final String id;
	private String email;
	private String referenceCurrency;
	private TimeZone timeZone;
	private boolean shortingEnabled;
	private int marginReservePercentage = 150;
	protected boolean parsingProperties = false;


	protected NewInstances<Strategy> strategies = new NewInstances<>(new Strategy[0]);
	protected NewInstances<StrategyMonitor> monitors = new NewInstances<>(new StrategyMonitor[0]);
	protected Instances<OrderListener> listeners = new Instances<>(new OrderListener[0]);

	Map<String, Allocation> allocations = new ConcurrentHashMap<>();

	Map<String, String[]> tradedPairs = new ConcurrentHashMap<>();
	Set<String> supportedSymbols = new TreeSet<>();
	Map<String, OrderManager> orderManagers = new ConcurrentHashMap<>();

	private final TreeSet<String> requiredPropertyNames = new TreeSet<>();

	protected AccountConfiguration(String id) {
		this.id = id;
	}

	protected final void readProperties(PropertyBasedConfiguration properties) {

	}

	protected final void readProperties(final String account, PropertyBasedConfiguration properties) {
		String accountProperty = properties.getPropertyNames().stream().filter(prop -> prop.startsWith(account)).findFirst().orElse(null);
		if (accountProperty == null) {
			return;
		}

		String accountId = account;
		parsingProperties = true;
		try {
			if (!accountId.isBlank()) {
				accountId = accountId + ".";
			}
			email = properties.getOptionalProperty(accountId + "email");
			referenceCurrency = properties.getProperty(accountId + "reference.currency");
			shortingEnabled = properties.getBoolean(accountId + "enable.shorting", false);
			marginReservePercentage = properties.getInteger(accountId + "margin.reserve.percentage", 150);

			String tz = properties.getOptionalProperty(accountId + "timezone");
			timeZone = getTimeZone(tz);
			if (timeZone == null) {
				String msg = "Unsupported timezone '" + tz + "' set ";
				if (accountId.isBlank()) {
					msg += "in 'timezone' property.";
				} else {
					msg += "for '" + accountId + "timezone' property.";
				}
				throw new IllegalConfigurationException(msg + supportedTimezoneDescription);
			}

			tradeWith(properties.getOptionalList(accountId + "asset.symbols").toArray(new String[0]));
			parseTradingPairs(properties, accountId + "trade.pairs", this::tradeWithPair);

			parseAllocationProperty(properties, accountId + "trade.minimum.amount", this::minimumInvestmentAmountPerTrade);
			parseAllocationProperty(properties, accountId + "trade.maximum.amount", this::maximumInvestmentAmountPerTrade);
			parseAllocationProperty(properties, accountId + "trade.maximum.percentage", this::maximumInvestmentPercentagePerTrade);
			parseAllocationProperty(properties, accountId + "asset.maximum.amount", this::maximumInvestmentAmountPerAsset);
			parseAllocationProperty(properties, accountId + "asset.maximum.percentage", this::maximumInvestmentPercentagePerAsset);


			Map<Class<?>, LinkedHashSet<String>> classesToSearch = new HashMap<>();
			classesToSearch.put(Strategy.class, properties.getOptionalSet(accountId + "strategies"));
			classesToSearch.put(StrategyMonitor.class, properties.getOptionalSet(accountId + "monitors"));
			classesToSearch.put(OrderListener.class, properties.getOptionalSet(accountId + "listeners"));

			classesToSearch.values().removeIf(Set::isEmpty);

			Map<Class<?>, LinkedHashSet<Class<?>>> searchResult = Utils.findClasses(classesToSearch);

			for (var e : searchResult.entrySet()) {
				if (e.getKey() == Strategy.class) {
					addImplementation(strategies, e.getValue());
				} else if (e.getKey() == StrategyMonitor.class) {
					addImplementation(monitors, e.getValue());
				} else if (e.getKey() == OrderListener.class) {
					addImplementation(listeners, e.getValue());
				}
			}
			parseInstancePerGroupProperty(properties, accountId + "order.manager", OrderManager.class, this::orderManager);

			//		strategies.addAll();
			//		monitors.addAll(properties.getOptionalList(accountId+"monitors"));
			//		listeners.addAll(properties.getOptionalList(accountId+"listeners"));

			readExchangeAccountProperties(accountId, properties);
		} finally {
			parsingProperties = false;
		}
	}

	private void addImplementation(AbstractNewInstances instances, Collection<Class<?>> classes) {
		for (Class c : classes) {
			instances.add(c);
		}
	}

	protected void readExchangeAccountProperties(String accountId, PropertyBasedConfiguration properties) {

	}

	private void parseTradingPairs(PropertyBasedConfiguration properties, String propertyName, Consumer<String[][]> consumer) {
		List<String> pairsList = properties.getOptionalList(propertyName);

		List<String[]> pairs = new ArrayList<>();
		pairsList.forEach(p -> pairs.add(StringUtils.split(p, '/')));

		consumer.accept(pairs.toArray(new String[0][]));
	}

	private void parseAllocationProperty(PropertyBasedConfiguration properties, String propertyName, BiConsumer<Double, String[]> consumer) {
		Function<String, Double> f = (String allocation) -> {
			try {
				return Double.valueOf(allocation);
			} catch (NumberFormatException ex) {
				throw new IllegalConfigurationException("Invalid allocation value '" + allocation + "' defined in property '" + propertyName + "'", ex);
			}
		};
		parseGroupSetting(properties, propertyName, f, consumer);
	}

	private <T> void parseInstancePerGroupProperty(PropertyBasedConfiguration properties, String propertyName, Class<T> parent, BiConsumer<T, String[]> consumer) {
		Function<String, T> f = (String className) -> {
			try {
				return findClassAndInstantiate(parent, className);
			} catch (Exception ex) {
				throw new IllegalConfigurationException("Error trying to instantiate object of type '" + className + "' defined in property '" + propertyName + "'", ex);
			}
		};
		parseGroupSetting(properties, propertyName, f, consumer);
	}

	public boolean isConfigured() {
		return StringUtils.isNoneBlank(referenceCurrency);
	}

	public String id() {
		return id;
	}

	public String email() {
		return email;
	}

	public T email(String email) {
		this.email = email;
		return (T) this;
	}

	public String referenceCurrency() {
		return referenceCurrency;
	}

	public T referenceCurrency(String referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		return (T) this;
	}

	public TimeZone timeZone() {
		return timeZone == null ? TimeZone.getDefault() : timeZone;
	}

	private TimeZone getTimeZone(String tz) {
		if (tz == null || tz.equalsIgnoreCase("system")) {
			return timeZone = TimeZone.getDefault();
		} else if (supportedTimeZones.contains(tz)) {
			return timeZone = TimeZone.getTimeZone(tz);
		}
		return null;
	}

	public T timeZone(String timeZone) {
		this.timeZone = getTimeZone(timeZone);
		if (this.timeZone == null) {
			throw new IllegalArgumentException("Unsupported time zone: '" + timeZone + "'." + supportedTimezoneDescription);
		}
		return (T) this;
	}

	public T timeZone(ZoneId zoneId) {
		this.timeZone = TimeZone.getTimeZone(zoneId);
		return (T) this;
	}

	public T timeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return (T) this;
	}

	public NewInstances<Strategy> strategies() {
		return strategies;
	}

	public NewInstances<StrategyMonitor> monitors() {
		return monitors;
	}

	public Instances<OrderListener> listeners() {
		return listeners;
	}

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
	public T maximumInvestmentPercentagePerAsset(double percentage, String... symbols) {
		return updateAllocation("percentage of account", percentage, symbols, (allocation) -> allocation.setMaximumPercentagePerAsset(percentage));
	}

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
	public T maximumInvestmentAmountPerAsset(double maximumAmount, String... symbols) {
		return updateAllocation("maximum expenditure of account", maximumAmount, symbols, (allocation) -> allocation.setMaximumAmountPerAsset(maximumAmount));
	}

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
	public T maximumInvestmentPercentagePerTrade(double percentage, String... symbols) {
		return updateAllocation("percentage of account per trade", percentage, symbols, (allocation) -> allocation.setMaximumPercentagePerTrade(percentage));
	}

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
	public T maximumInvestmentAmountPerTrade(double maximumAmount, String... symbols) {
		return updateAllocation("maximum expenditure per trade", maximumAmount, symbols, (allocation) -> allocation.setMaximumAmountPerTrade(maximumAmount));
	}

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
	public T minimumInvestmentAmountPerTrade(double minimumAmount, String... symbols) {
		return updateAllocation("minimum expenditure per trade", minimumAmount, symbols, (allocation) -> allocation.setMinimumAmountPerTrade(minimumAmount));
	}

	private T updateAllocation(String description, double param, String[] symbols, Function<Allocation, Allocation> f) {
		if (symbols.length == 0) {
			symbols = supportedSymbols.toArray(new String[0]);
		}
		for (String symbol : symbols) {
			if (supportedSymbols.contains(symbol) || parsingProperties) {
				allocations.compute(symbol, (s, allocation) -> allocation == null ? f.apply(new Allocation()) : f.apply(allocation));
//				Balance.balanceUpdateCounts.clear();
			} else {
				reportUnknownSymbol("Can't allocate " + description + " for '" + symbol + "' to " + param, symbol);
			}
		}
		return (T) this;
	}


	/**
	 * Returns the maximum investment percentage, relative to the whole account balance, to be invested in a given asset.
	 * E.g. if the account balance is $1000.00 and the percentage is set to 20.0, the account will never buy more
	 * than $200.00 worth of an asset and the remaining $800 will be used for other assets.
	 *
	 * @param assetSymbol symbol of the asset to query (e.g. BTC, EUR, MSFT, etc).
	 *
	 * @return the maximum percentage allowed to be spent on any given trade for the given asset (from 0.0 to 100.0).
	 */
	public double maximumInvestmentPercentagePerAsset(String assetSymbol) {
		return allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumPercentagePerAsset();
	}

	/**
	 * Returns the maximum investment amount to allocate to a given symbol. E.g. if the account balance is $1000.00
	 * and the amount is set to $400.00, the account will never buy more than $400.00 of an asset. The remaining $600 of the balance will be used
	 * for other assets.
	 *
	 * @param assetSymbol symbol of the asset to query (e.g. BTC, EUR, MSFT, etc).
	 *
	 * @return the maximum amount of funds to allocate to the given asset.
	 */
	public double maximumInvestmentAmountPerAsset(String assetSymbol) {
		return allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumAmountPerAsset();
	}

	/**
	 * Returns the a maximum percentage of funds to be used in a single trade. The percentage is relative to the whole account balance.
	 * E.g.  if the account balance is $1000.00 and the percentage is set to 5.0, the account will never buy more than $50.00
	 * at once. If another buy signal is received to buy into the same asset, that next purchase will be limited to the maximum 5% of the account
	 * balance as well, and so on.
	 *
	 * @param assetSymbol symbol of the asset to query (e.g. BTC, EUR, MSFT, etc).
	 *
	 * @return the maximum percentage of the account balance allowed to be spent on any given trade for the given asset (from 0.0 to 100.0).
	 */
	public double maximumInvestmentPercentagePerTrade(String assetSymbol) {
		return allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumPercentagePerTrade();
	}

	/**
	 * Returns the maximum amount of funds to be used in a single trade for a given asset.
	 * E.g.  if the account balance is $1000.00 and the maximum amount per trade is set to $100.0, the account will never buy more than $100.00
	 * at once. If another buy signal is received to buy into the same asset, that next purchase will be limited to the maximum $100.00, and so on.
	 *
	 * @param assetSymbol symbol of the asset to query (e.g. BTC, EUR, MSFT, etc).
	 *
	 * @return the maximum amount required to spend on any given trade for the given asset.
	 */
	public double maximumInvestmentAmountPerTrade(String assetSymbol) {
		return allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumAmountPerTrade();
	}

	/**
	 * Returns the minimum amount of funds to be used in a single trade for a given asset.
	 * E.g. if the minimum amount is set to $10.00, then the {@link Trader} will never open a buy order that is worh less than $10.00.
	 *
	 * @param assetSymbol symbol of the asset to query (e.g. BTC, EUR, MSFT, etc).
	 *
	 * @return the minimum amount required to spend on any given trade for the given asset.
	 */
	public double minimumInvestmentAmountPerTrade(String assetSymbol) {
		return allocations.getOrDefault(assetSymbol, NO_LIMITS).getMinimumAmountPerTrade();
	}

	public boolean isSymbolSupported(String assetOrFundSymbol) {
		return symbols().contains(assetOrFundSymbol) || assetOrFundSymbol.equals(referenceCurrency);
	}

	public Set<String> symbols() {
		return supportedSymbols;
	}

	public Collection<String[]> tradedWithPairs() {
		return tradedPairs.values();
	}

	/**
	 * Assigns an {@link OrderManager} for the given symbols. By default, the {@link DefaultOrderManager} will be used when trading all symbols of
	 * this account. Use this method to replace with your own {@link OrderManager} implementation.
	 *
	 * @param orderManager the order manager to be used to control the lifecycle of trades made for the given symbols
	 * @param symbols      the specific symbols which should use the given order manager. If none given then the order manager be used to manage all
	 *                     orders made for the symbols traded by this account (e.g. BTCUSDT, EURJPY, MSFTUSD, etc).
	 *
	 * @return this configuration object, for further settings.
	 */
	public T orderManager(OrderManager orderManager, String... symbols) {
		if (symbols.length == 0) {
			symbols = tradedPairs.keySet().toArray(new String[0]);
		}
		if (symbols.length == 0) {
			throw new IllegalArgumentException("Can't associate order manager " + orderManager + " before configuring the trading symbols to be used");
		}
		for (String symbol : symbols) {
			this.orderManagers.put(symbol, orderManager);
		}
		return (T) this;
	}

	/**
	 * Returns the {@link OrderManager} associated with the given symbol. In none defined explicitly, the {@link DefaultOrderManager} will be
	 * returned.
	 *
	 * @param symbol symbol of the ticker symbol to query (e.g. BTCUSDT, EURJPY, MSFTUSD, etc).
	 *
	 * @return the order manager to be used to control the lifecycle of trades made for the given symbol
	 */
	public OrderManager orderManager(String symbol) {
		return orderManagers.getOrDefault(symbol, DEFAULT_ORDER_MANAGER);
	}

	public T tradedWithPairs(Collection<String[]> symbols) {
		symbols.forEach(p -> {
			tradeWithPair(p[0], p[1]);
		});
		return (T) this;
	}

	public T tradeWithPair(String[]... symbolPairs) {
		for (String[] pair : symbolPairs) {
			tradeWithPair(pair[0], pair[1]);
		}
		return (T) this;
	}

	public T tradeWith(String... assetSymbols) {
		for (String assetSymbol : assetSymbols) {
			tradeWith(assetSymbol);
		}
		return (T) this;
	}

	public T tradeWith(String assetSymbol) {
		return tradeWithPair(assetSymbol, referenceCurrency);
	}

	public T tradeWithPair(String assetSymbol, String fundSymbol) {
		tradedPairs.put(assetSymbol + fundSymbol, new String[]{assetSymbol, fundSymbol});
		supportedSymbols.add(assetSymbol);
		supportedSymbols.add(fundSymbol);
		return (T) this;
	}

	public Map<String, String[]> symbolPairs() {
		return Collections.unmodifiableMap(tradedPairs);
	}

	public T clearTradingPairs() {
		tradedPairs.clear();
		supportedSymbols.clear();
		return (T) this;
	}

	@Override
	public T clone() {
		try {
			T out = (T) super.clone();
			out.strategies = strategies.clone();
			out.monitors = monitors.clone();
			out.listeners = listeners.clone();
			out.allocations = new ConcurrentHashMap<>();
			allocations.forEach((k, v) -> out.allocations.put(k, v.clone()));
			out.tradedPairs = new ConcurrentHashMap<>(tradedPairs);
			out.supportedSymbols = new TreeSet<>(supportedSymbols);
			out.orderManagers = new ConcurrentHashMap<>(orderManagers);
			return out;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	//
//	private Collection<String[]> populateTradingPairs() {
//		return symbolPairs.values();

	//List<String[]> out = new ArrayList<>();

//		Set<String> tradedSymbols = new HashSet<>();
//		for (String[] symbol : symbols) {
//			out.add(symbol);
//			tradedSymbols.add(symbol[0]);
//		}

//		enable this later - used to allow switching straight into another asset without selling into cash then buying the desired asset with that cash.
//		for (String[] symbol : symbols) {
//			for (String mainTradeSymbol : mainTradeSymbols) {
//				if (tradedSymbols.contains(mainTradeSymbol) && !symbol[0].equals(mainTradeSymbol) && !symbol[1].equals(mainTradeSymbol)) {
//					symbol = symbol.clone();
//					symbol[1] = mainTradeSymbol;
//					out.add(symbol);
//				}
//			}
//		}
//		return out;
//	}

	public IllegalArgumentException reportUnknownSymbol(String symbol) {
		throw reportUnknownSymbol(null, symbol);
	}

	public IllegalArgumentException reportUnknownSymbol(String message, String symbol) {
		String msg = "Account '" + id + "' is not managing '" + symbol + "'. Allowed symbols are: " + StringUtils.join(symbols(), ", ") + " and " + referenceCurrency();
		if (message != null) {
			throw new IllegalArgumentException(message + ". " + msg);
		} else {
			throw new IllegalArgumentException(msg);
		}
	}

	public T enableShorting() {
		shortingEnabled = true;
		return (T) this;
	}

	public T disableShorting() {
		shortingEnabled = false;
		return (T) this;
	}

	public boolean shortingEnabled() {
		return shortingEnabled;
	}

	public T marginReservePercentage(int marginReservePercentage) {
		if (marginReservePercentage < 100) {
			throw new IllegalArgumentException("Margin reserve percentage must be at least 100%");
		}
		this.marginReservePercentage = marginReservePercentage;
		return (T) this;
	}

	public int marginReservePercentage() {
		return marginReservePercentage;
	}

	Set<String> getRequiredPropertyNames() {
		requiredPropertyNames.add("reference.currency");
		return requiredPropertyNames;
	}

	protected void addRequiredPropertyNames(String... names) {
		Collections.addAll(requiredPropertyNames, names);
	}
}