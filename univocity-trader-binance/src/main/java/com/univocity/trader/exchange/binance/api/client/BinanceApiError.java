package com.univocity.trader.exchange.binance.api.client;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import org.apache.commons.lang3.builder.*;

/**
 * Binance API error object.
 */
public class BinanceApiError {

	/**
	 * Error code.
	 */
	private int code;

	/**
	 * Error message.
	 */
	private String msg;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("code", code)
				.append("msg", msg)
				.toString();
	}
}
