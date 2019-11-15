package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * An asset balance in an Account.
 *
 * @see Account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBalance {

	/**
	 * Asset symbol.
	 */
	private String asset;

	/**
	 * Available balance.
	 */
	private String free;

	/**
	 * Locked by open orders.
	 */
	private String locked;

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	public String getLocked() {
		return locked;
	}

	public void setLocked(String locked) {
		this.locked = locked;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("asset", asset)
				.append("free", free)
				.append("locked", locked)
				.toString();
	}

	public double getFreeAmount() {
		return toAmount(free);
	}

	public double getLockedAmount() {
		return toAmount(locked);
	}

	private double toAmount(String s) {
		try {
			return s == null ? 0.0 : Double.parseDouble(s);
		} catch (Exception e) {
			return 0.0;
		}
	}

	public static double getFreeAmount(AssetBalance asset) {
		if (asset == null) {
			return 0.0;
		}
		return asset.getFreeAmount();
	}


	public static double getLockedAmount(AssetBalance asset) {
		if (asset == null) {
			return 0.0;
		}
		return asset.getLockedAmount();
	}

	public static double getFreeAmount(Map<String, AssetBalance> balances, String asset) {
		return getFreeAmount(balances.get(asset));
	}

	public static double getLockedAmount(Map<String, AssetBalance> balances, String asset) {
		return getLockedAmount(balances.get(asset));
	}
}
