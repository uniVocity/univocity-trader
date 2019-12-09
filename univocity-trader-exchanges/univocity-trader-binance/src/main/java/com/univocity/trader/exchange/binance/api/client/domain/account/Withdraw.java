package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * A withdraw that was done to a Binance account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Withdraw {

	/**
	 * Amount withdrawn.
	 */
	private String amount;

	/**
	 * Destination address.
	 */
	private String address;

	/**
	 * Symbol.
	 */
	private String asset;

	private String applyTime;

	private String successTime;

	/**
	 * Transaction id.
	 */
	private String txId;

	/**
	 * Id.
	 */
	private String id;

	/**
	 * (0:Email Sent,1:Cancelled 2:Awaiting Approval 3:Rejected 4:Processing 5:Failure 6:Completed)
	 */
	private int status;

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(String applyTime) {
		this.applyTime = applyTime;
	}

	public String getSuccessTime() {
		return successTime;
	}

	public void setSuccessTime(String successTime) {
		this.successTime = successTime;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
				.append("address", address)
				.append("asset", asset)
				.append("applyTime", applyTime)
				.append("successTime", successTime)
				.append("txId", txId)
				.append("id", id)
				.append("status", status)
				.toString();
	}
}
