package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * History of account deposits.
 *
 * @see Deposit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositHistory {

	@JsonProperty("depositList")
	private List<Deposit> depositList;

	private boolean success;

	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<Deposit> getDepositList() {
		return depositList;
	}

	public void setDepositList(List<Deposit> depositList) {
		this.depositList = depositList;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("depositList", depositList)
				.append("success", success)
				.append("msg", msg)
				.toString();
	}
}
