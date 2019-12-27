package com.univocity.trader.config;

import com.univocity.trader.simulation.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.config.Utils.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Simulation implements ConfigurationGroup, Cloneable {

	private static DateTimeFormatter newFormatter(String pattern) {
		return new DateTimeFormatterBuilder()
				.appendPattern(pattern)
				.parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
				.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter();
	}

	private static final DateTimeFormatter[] formatters = new DateTimeFormatter[]{
			newFormatter("yyyy-MM-dd HH:mm"),
			newFormatter("yyyy-MM-dd"),
			newFormatter("yyyy-MM"),
			newFormatter("yyyy"),
	};


	private LocalDateTime simulationStart;
	private LocalDateTime simulationEnd;
	private boolean cacheCandles = false;
	private int activeQueryLimit = 15;
	private int backfillLength = 6;
	private ChronoUnit backfillUnit = ChronoUnit.MONTHS;

	private Map<String, Double> initialFunds = new ConcurrentHashMap<>();
	private final List<Parameters> parameters = new ArrayList<>();

	public final LocalDateTime simulationStart() {
		return simulationStart != null ? simulationStart : LocalDateTime.now().minusYears(1);
	}

	public final LocalDateTime simulationEnd() {
		return simulationEnd != null ? simulationEnd : LocalDateTime.now();
	}

	public Simulation simulationStart(LocalDateTime simulationStart) {
		this.simulationStart = simulationStart;
		return this;
	}

	public Simulation simulationEnd(LocalDateTime simulationEnd) {
		this.simulationEnd = simulationEnd;
		return this;
	}

	public Simulation simulationStart(String simulationStart) {
		this.simulationStart = parseDateTime(simulationStart, null);
		return this;
	}

	public Simulation simulationEnd(String simulationEnd) {
		this.simulationEnd = parseDateTime(simulationEnd, null);
		return this;
	}

	public Simulation simulationStart(long time) {
		this.simulationStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationEnd(long time) {
		this.simulationEnd = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationStart(Instant date) {
		this.simulationStart = date == null ? null : LocalDateTime.ofInstant(date, ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationEnd(Instant date) {
		this.simulationEnd = date == null ? null : LocalDateTime.ofInstant(date, ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationStart(Date date) {
		this.simulationStart = date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationEnd(Date date) {
		this.simulationEnd = date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return this;
	}


	public Simulation simulationStart(Calendar date) {
		this.simulationStart = date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return this;
	}

	public Simulation simulationEnd(Calendar date) {
		this.simulationEnd = date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return this;
	}

	@Override
	public void readProperties(PropertyBasedConfiguration properties) {
		simulationStart(parseDateTime(properties, "simulation.start"));
		simulationEnd(parseDateTime(properties, "simulation.end"));
		cacheCandles(properties.getBoolean("simulation.cache.candles", false));
		activeQueryLimit(properties.getInteger("simulation.active.query.limit", 15));


		String backfill = properties.getOptionalProperty("simulation.history.backfill");
		if (backfill != null) {
			char ch = Character.toUpperCase(backfill.charAt(backfill.length() - 1));
			if (Character.isLetter(ch)) {
				backfill = backfill.substring(0, backfill.length() - 1);
				this.backfillUnit = getBackfillUnitFromLetter(ch);
			} else {
				this.backfillUnit = ChronoUnit.DAYS;
			}
			this.backfillLength = Integer.parseInt(backfill);
		}

		parseInitialFunds(properties);

		String pathToParameters = properties.getOptionalProperty("simulation.parameters.file");
		if (pathToParameters != null) {
			File parametersFile = properties.getValidatedFile("simulation.parameters.file", true, true, false, false);
			String className = properties.getProperty("simulation.parameters.class");
			Class<? extends Parameters> parametersClass = Utils.findClass(Parameters.class, className);
			parameters(parametersFile, parametersClass);
		}
	}

	private ChronoUnit getBackfillUnitFromLetter(char ch) {
		switch (ch) {
			case 'Y':
				return ChronoUnit.YEARS;
			case 'M':
				return ChronoUnit.MONTHS;
			case 'W':
				return ChronoUnit.WEEKS;
			case 'D':
				return ChronoUnit.DAYS;
		}
		throw new IllegalConfigurationException("Invalid backfill length unit '" + ch + "'. Expected one of: Y, M, W, D");
	}

	public Simulation backfillDays(int lengthInDays) {
		backfillLength = lengthInDays;
		backfillUnit = ChronoUnit.DAYS;
		return this;
	}

	public Simulation backfillWeeks(int lengthInWeeks) {
		backfillLength = lengthInWeeks;
		backfillUnit = ChronoUnit.WEEKS;
		return this;
	}

	public Simulation backfillMonths(int lengthInMonths) {
		backfillLength = lengthInMonths;
		backfillUnit = ChronoUnit.MONTHS;
		return this;
	}

	public Simulation backfillYears(int lengthInYears) {
		backfillLength = lengthInYears;
		backfillUnit = ChronoUnit.YEARS;
		return this;
	}

	public int getBackfillLength() {
		return backfillLength;
	}

	public void parameters(String pathToParametersFile, Class<? extends Parameters> typeOfParameters) {
		parameters(new File(pathToParametersFile), typeOfParameters);
	}

	public Simulation parameters(File parametersFile, Class<? extends Parameters> typeOfParameters) {
		loadParameters(parametersFile, typeOfParameters);
		return this;
	}

	private void parseInitialFunds(PropertyBasedConfiguration properties) {
		Function<String, Double> f = (amount) -> {
			try {
				return Double.valueOf(amount);
			} catch (NumberFormatException ex) {
				throw new IllegalConfigurationException("Invalid initial funds amount '" + amount + "' defined in property 'simulation.initial.funds'", ex);
			}
		};
		parseGroupSetting(properties, "simulation.initial.funds", f, this::initialAmounts);
	}

	private LocalDateTime parseDateTime(String s, String propertyName) {
		if (StringUtils.isBlank(s)) {
			return null;
		}

		for (DateTimeFormatter formatter : formatters) {
			try {
				return LocalDateTime.parse(s, formatter);
			} catch (Exception e) {
				//ignore
			}
		}

		String property = propertyName == null ? "" : " of property '" + propertyName + "'";
		throw new IllegalConfigurationException("Unrecognized date format in value '" + s + "'" + property + ". Supported formats are: yyyy-MM-dd HH:mm, yyyy-MM-dd, yyyy-MM and yyyy");
	}

	private LocalDateTime parseDateTime(PropertyBasedConfiguration properties, String propertyName) {
		return parseDateTime(properties.getOptionalProperty(propertyName), propertyName);
	}

	public boolean cacheCandles() {
		return cacheCandles;
	}

	public Simulation cacheCandles(boolean cacheCandles) {
		this.cacheCandles = cacheCandles;
		return this;
	}

	public Simulation initialFunds(double initialFunds) {
		initialAmount("", initialFunds);
		return this;
	}

	public double initialFunds() {
		return initialAmount("");
	}

	public double initialAmount(String symbol) {
		return initialFunds.getOrDefault(symbol, 0.0);
	}

	public Simulation initialAmount(String symbol, double initialAmount) {
		initialFunds.put(symbol, initialAmount);
		return this;
	}

	public Map<String, Double> initialAmounts() {
		return Collections.unmodifiableMap(initialFunds);
	}

	private void initialAmounts(double initialAmount, String... symbols) {
		if (symbols.length == 0) {
			//default to reference currency.
			initialFunds(initialAmount);
		} else {
			for (String symbol : symbols) {
				initialFunds.put(symbol, initialAmount);
			}
		}
	}

	@Override
	public boolean isConfigured() {
		return !initialFunds.isEmpty();
	}

	@Override
	public Simulation clone() {
		try {
			Simulation out = (Simulation) super.clone();
			out.initialFunds = new ConcurrentHashMap<>(initialFunds);
			return out;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	public int activeQueryLimit() {
		return activeQueryLimit;
	}

	public Simulation activeQueryLimit(int activeQueryLimit) {
		this.activeQueryLimit = activeQueryLimit;
		return this;
	}

	private void loadParameters(File parametersFile, Class<? extends Parameters> typeOfParameters) {
		//TODO: load with univocity-parsers
	}

	public List<Parameters> parameters() {
		return parameters;
	}

	public Simulation addParameters(Collection<Parameters> parameters) {
		if(parameters != null) {
			this.parameters.addAll(parameters);
		}
		return this;
	}

	public Simulation addParameters(Parameters parameters) {
		this.parameters.add(parameters);
		return this;
	}

	public Simulation clearParameters() {
		this.parameters.clear();
		return this;
	}

	public Instant backfillStart() {
		return LocalDate.now().minus(backfillLength, backfillUnit).atStartOfDay().toInstant(ZoneOffset.UTC);
	}
}
