package com.univocity.trader.config;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.config.Utils.*;

/**
 * Basic configuration on an account. Allows to define maximum a minimum investment amounts to one or more symbols,
 * as well as assigning {@link OrderManager}s to different symbols.
 */
public abstract class AccountConfiguration<T extends AccountConfiguration<T>> extends AbstractTradingGroup<T> {

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

	private String email;
	private TimeZone timeZone;

	private final TreeSet<String> requiredPropertyNames = new TreeSet<>();
	protected final Map<String, TradingGroup> tradingGroups = new HashMap<>();
	int marginReservePercentage = 150;

	protected AccountConfiguration(String id) {
		super(id);
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

	@Override
	public boolean isConfigured() {
		if (tradingGroups.isEmpty()) {
			return super.isConfigured();
		} else {
			boolean configured = false;
			for (TradingGroup group : tradingGroups.values()) {
				configured |= group.isConfigured();
			}
			return configured;
		}
	}

	public String email() {
		return email;
	}

	public T email(String email) {
		this.email = email;
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

	@Override
	public T clone(boolean deep) {
		T out = (T) super.clone(deep);
		this.tradingGroups.forEach((k, v) -> out.tradingGroups.put(k, (TradingGroup) v.clone(deep)));
		return out;
	}

	@Override
	public T clone() {
		return (T) super.clone();
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
		requiredPropertyNames.add("strategies");
		return requiredPropertyNames;
	}

	protected void addRequiredPropertyNames(String... names) {
		Collections.addAll(requiredPropertyNames, names);
	}

	public TradingGroup tradingGroup(String id) {
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("Trading group ID can't be empty");
		}
		return tradingGroups.computeIfAbsent(id, this::newTradingGroup);
	}

	public Collection<TradingGroup> tradingGroups() {
		return Collections.unmodifiableCollection(tradingGroups.values());
	}

	private TradingGroup newTradingGroup(String id) {
		TradingGroup out = new TradingGroup(id);
		out.copyFrom(this);
		return out;
	}

	@Override
	public Set<String> symbols() {
		if(supportedSymbols.isEmpty()){
			supportedSymbols.addAll(super.symbols());
			for (TradingGroup tradingGroup : tradingGroups.values()) {
				supportedSymbols.addAll(tradingGroup.symbols());
			}
		}
		return supportedSymbols;
	}

	public Map<String, String[]> getAllSymbolPairs() {
		Map<String, String[]> out = new TreeMap<>();
		for (TradingGroup tradingGroup : tradingGroups.values()) {
			out.putAll(tradingGroup.symbolPairs());
		}
		out.putAll(symbolPairs());
		return out;
	}
}