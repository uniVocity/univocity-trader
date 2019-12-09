package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * A withdraw result that was done to a Binance account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawResult {

	/**
	 * Withdraw message.
	 */
	private String msg;

	/**
	 * Withdraw success.
	 */
	private boolean success;

	/**
	 * Withdraw id.
	 */
	private String id;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("msg", msg)
				.append("success", success)
				.append("id", id)
				.toString();
	}


}
