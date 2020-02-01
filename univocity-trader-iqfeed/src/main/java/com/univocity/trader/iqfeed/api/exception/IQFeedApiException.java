package com.univocity.trader.iqfeed.api.exception;

import com.univocity.trader.iqfeed.api.*;

public class IQFeedApiException extends RuntimeException {
	private IQFeedApiError error;

	public IQFeedApiException(IQFeedApiError error) {
		this.error = error;
	}

	public IQFeedApiException() {
		super();
	}

	public IQFeedApiException(String message) {
		super(message);
	}

	public IQFeedApiException(Throwable cause) {
		super(cause);
	}

	public IQFeedApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public IQFeedApiError getError() {
		return error;
	}

	@Override
	public String getMessage() {
		if (error != null) {
			return error.getMsg();
		}
		return super.getMessage();
	}
}
