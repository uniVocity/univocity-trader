package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.candles.SymbolInformation.*;

public class SymbolPriceDetails {

	private static final SymbolPriceDetails NOOP = new SymbolPriceDetails(null, Collections.emptyMap());

	private final Map<String, SymbolPriceDetails> allFormatters;

	private final SymbolInformation info;

	public SymbolPriceDetails(Exchange<?, ?> exchange) {
		this(exchange, null);
	}

	public SymbolPriceDetails(Exchange<?, ?> exchange, String referenceCurrency) {
		Map<String, SymbolInformation> symbols = exchange.getSymbolInformation();

		this.allFormatters = new ConcurrentHashMap<>();
		if (symbols != null) {
			symbols.forEach((s, i) -> allFormatters.put(s, new SymbolPriceDetails(i, allFormatters)));
		}

		this.info = null;

		if (referenceCurrency != null) {
			populateDetailsForReferenceCurrency(referenceCurrency);
		}
	}

	private void populateDetailsForReferenceCurrency(String referenceSymbol) {
		Set<String> existing = allFormatters.keySet();
		for (String symbol : existing) {
			if (!symbol.startsWith(referenceSymbol) && symbol.endsWith(referenceSymbol)) {
				SymbolPriceDetails details = allFormatters.get(symbol);
				SymbolInformation info = details.info;

				String asset = symbol.substring(0, symbol.length() - referenceSymbol.length());
				String inverseSymbol = referenceSymbol + asset;
				SymbolInformation inverseInfo = new SymbolInformation(inverseSymbol);

				inverseInfo.priceDecimalPlaces(info.quantityDecimalPlaces());
				inverseInfo.quantityDecimalPlaces(info.priceDecimalPlaces());

				SymbolPriceDetails inverseDetails = new SymbolPriceDetails(info, allFormatters);
				allFormatters.put(inverseSymbol, inverseDetails);
			}
		}
	}

	private SymbolPriceDetails(SymbolInformation info, Map<String, SymbolPriceDetails> allFormatters) {
		this.info = info;
		this.allFormatters = allFormatters;
	}

	public SymbolPriceDetails switchToSymbol(String symbol) {
		SymbolPriceDetails out = allFormatters.get(symbol);
		if (out == null) {
			return NOOP;
		}
		return out;
	}

	private <T> T getOrDefault(SymbolInformation info, Function<SymbolInformation, T> function, T defaultValue) {
		if (info != null) {
			info = this.info;
			if (info != null) {
				return function.apply(info);
			}
		}
		return defaultValue;
	}

	private int getPriceDecimals(SymbolInformation info) {
		return getOrDefault(info, SymbolInformation::priceDecimalPlaces, Balance.ROUND_MC.getPrecision());
	}

	public BigDecimal getMinimumOrderAmount(BigDecimal unitPrice) {
		return getOrDefault(info, SymbolInformation::minimumAssetsPerOrder, DEFAULT_MINIMUM_ASSETS_PER_ORDER).multiply(unitPrice);
	}

	public double getMinimumOrderAmount(double unitPrice) {
		return getOrDefault(info, SymbolInformation::minimumAssetsPerOrderAmount, DEFAULT_MINIMUM_ASSETS_PER_ORDER_AMOUNT) * unitPrice;
	}

	private int getQuantityDecimals(SymbolInformation info) {
		return getOrDefault(info, SymbolInformation::quantityDecimalPlaces, Balance.ROUND_MC.getPrecision());
	}

	public String quantityToString(double quantity) {
		return toString(getQuantityDecimals(info), quantity);
	}

	public String priceToString(double price) {
		return toString(getPriceDecimals(info), price);
	}

	public String quantityToString(BigDecimal quantity) {
		return toString(getQuantityDecimals(info), quantity);
	}

	public String priceToString(BigDecimal quantity) {
		return toString(getPriceDecimals(info), quantity);
	}

	public static String toString(int decimals, double quantity) {
		return toBigDecimal(decimals, quantity).toPlainString();
	}

	public static String toString(int decimals, BigDecimal value) {
		if(value == null){
			return null;
		}
		return value.setScale(decimals, RoundingMode.FLOOR).toPlainString();
	}

	public static BigDecimal toBigDecimal(int decimals, double quantity) {
		BigDecimal bd = BigDecimal.valueOf(quantity);
		bd = bd.setScale(decimals, RoundingMode.FLOOR);
		return bd;
	}

	public static BigDecimal updateScale(int decimals, BigDecimal amount) {
		return amount.setScale(decimals, RoundingMode.FLOOR);
	}

	public BigDecimal adjustQuantityScale(double quantity) {
		return toBigDecimal(getQuantityDecimals(info), quantity);
	}

	public BigDecimal priceToBigDecimal(double price) {
		return toBigDecimal(getPriceDecimals(info), price);
	}

	public BigDecimal adjustQuantityScale(BigDecimal quantity) {
		return updateScale(getQuantityDecimals(info), quantity);
	}

	public BigDecimal adjustPriceScale(BigDecimal price) {
		return updateScale(getPriceDecimals(info), price);
	}

	public int pipSize() {
		return getOrDefault(info, SymbolInformation::priceDecimalPlaces, 0);
	}
}
