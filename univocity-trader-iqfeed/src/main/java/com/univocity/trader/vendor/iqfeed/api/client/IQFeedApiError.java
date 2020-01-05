package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.constant.*;
import org.apache.commons.lang3.builder.*;

public class IQFeedApiError {

	private int code;
	private String msg;

	@Override
	public String toString() {
		return new ToStringBuilder(this, IQFeedApiConstants.TO_STRING_BUILDER_STYLE)
				.append("code", code)
				.append("msg", msg)
				.toString();
	}

	public String getMsg() {
		return this.toString();
	}
}
