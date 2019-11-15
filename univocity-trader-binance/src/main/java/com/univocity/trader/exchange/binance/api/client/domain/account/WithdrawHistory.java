package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * History of account withdrawals.
 *
 * @see Withdraw
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawHistory {

	private List<Withdraw> withdrawList;

	private boolean success;

	public List<Withdraw> getWithdrawList() {
		return withdrawList;
	}

	public void setWithdrawList(List<Withdraw> withdrawList) {
		this.withdrawList = withdrawList;
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
				.append("withdrawList", withdrawList)
				.append("success", success)
				.toString();
	}
}
