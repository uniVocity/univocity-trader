package com.univocity.trader.exchange.binance.api.client.impl;

import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import retrofit2.*;

import java.io.*;

import static com.univocity.trader.exchange.binance.api.client.impl.BinanceApiServiceGenerator.*;

/**
 * An adapter/wrapper which transforms a Callback from Retrofit into a BinanceApiCallback which is exposed to the client.
 */
public class BinanceApiCallbackAdapter<T> implements Callback<T> {

	private final BinanceApiCallback<T> callback;

	public BinanceApiCallbackAdapter(BinanceApiCallback<T> callback) {
		this.callback = callback;
	}

	public void onResponse(Call<T> call, Response<T> response) {
		if (response.isSuccessful()) {
			callback.onResponse(response.body());
		} else {
			if (response.code() == 504) {
				// HTTP 504 return code is used when the API successfully sent the message but not get a response within the timeout period.
				// It is important to NOT treat this as a failure; the execution status is UNKNOWN and could have been a success.
				return;
			}
			try {
				BinanceApiError apiError = getBinanceApiError(response);
				onFailure(call, new BinanceApiException(apiError));
			} catch (IOException e) {
				onFailure(call, new BinanceApiException(e));
			}
		}
	}

	@Override
	public void onFailure(Call<T> call, Throwable throwable) {
		if (throwable instanceof BinanceApiException) {
			callback.onFailure(throwable);
		} else {
			callback.onFailure(new BinanceApiException(throwable));
		}
	}
}
