package com.univocity.trader.exchange.binance.api.client.domain.general;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * An asset Binance supports.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asset {

	@JsonProperty("id")
	private String id;

	@JsonProperty("assetCode")
	private String assetCode;

	@JsonProperty("assetName")
	private String assetName;

	@JsonProperty("unit")
	private String unit;

	@JsonProperty("transactionFee")
	private String transactionFee;

	@JsonProperty("commissionRate")
	private String commissionRate;

	@JsonProperty("freeAuditWithdrawAmt")
	private String freeAuditWithdrawAmount;

	@JsonProperty("freeUserChargeAmount")
	private String freeUserChargeAmount;

	@JsonProperty("minProductWithdraw")
	private String minProductWithdraw;

	@JsonProperty("withdrawIntegerMultiple")
	private String withdrawIntegerMultiple;

	@JsonProperty("confirmTimes")
	private long confirmTimes;

	@JsonProperty("enableWithdraw")
	private boolean enableWithdraw;

	@JsonProperty("isLegalMoney")
	private boolean isLegalMoney;

	public String getId() {
		return id;
	}

	public String getAssetCode() {
		return assetCode;
	}

	public String getAssetName() {
		return assetName;
	}

	public String getUnit() {
		return unit;
	}

	public String getTransactionFee() {
		return transactionFee;
	}

	public String getCommissionRate() {
		return commissionRate;
	}

	public String getFreeAuditWithdrawAmount() {
		return freeAuditWithdrawAmount;
	}

	public String getFreeUserChargeAmount() {
		return freeUserChargeAmount;
	}

	public String minProductWithdraw() {
		return minProductWithdraw;
	}

	public String getWithdrawIntegerMultiple() {
		return withdrawIntegerMultiple;
	}

	public long getConfirmTimes() {
		return confirmTimes;
	}

	public boolean canWithraw() {
		return enableWithdraw;
	}

	public boolean isLegalMoney() {
		return isLegalMoney;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("id", id)
				.append("assetCode", assetCode)
				.append("assetName", assetName)
				.append("unit", unit)
				.append("transactionFee", transactionFee)
				.append("commissionRate", commissionRate)
				.append("freeAuditWithdrawAmount", freeAuditWithdrawAmount)
				.append("freeUserChargeAmount", freeUserChargeAmount)
				.append("minProductWithdraw", minProductWithdraw)
				.append("withdrawIntegerMultiple", withdrawIntegerMultiple)
				.append("confirmTimes", confirmTimes)
				.append("enableWithdraw", enableWithdraw)
				.append("isLegalMoney", isLegalMoney)
				.toString();
	}
}
