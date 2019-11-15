package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * A deposit that was done to a Binance account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deposit {

	/**
	 * Amount deposited.
	 */
	private String amount;

	/**
	 * Symbol.
	 */
	private String asset;

	/**
	 * Deposit time.
	 */
	private String insertTime;

	/**
	 * Transaction id
	 */
	private String txId;

	/**
	 * (0:pending,1:success)
	 */
	private int status;

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(String insertTime) {
		this.insertTime = insertTime;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("amount", amount)
				.append("asset", asset)
				.append("insertTime", insertTime)
				.append("txId", txId)
				.append("status", status)
				.toString();
	}
}
