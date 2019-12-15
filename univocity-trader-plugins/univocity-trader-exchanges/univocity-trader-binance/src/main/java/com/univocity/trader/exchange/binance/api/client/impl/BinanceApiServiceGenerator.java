package com.univocity.trader.exchange.binance.api.client.impl;

import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import okhttp3.*;
import org.asynchttpclient.*;
import org.asynchttpclient.extras.retrofit.*;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.*;
import retrofit2.converter.jackson.*;

import java.io.*;
import java.lang.annotation.*;

/**
 * Generates a Binance API implementation based on @see {@link BinanceApiService}.
 */
public class BinanceApiServiceGenerator {

	private static final Converter.Factory converterFactory = JacksonConverterFactory.create();

	@SuppressWarnings("unchecked")
	private static final Converter<ResponseBody, BinanceApiError> errorBodyConverter =
			(Converter<ResponseBody, BinanceApiError>) converterFactory.responseBodyConverter(
					BinanceApiError.class, new Annotation[0], null);

	public static <S> S createService(Class<S> serviceClass, AsyncHttpClient httpClient) {
		return createService(serviceClass, httpClient, null, null);
	}

	public static <S> S createService(Class<S> serviceClass, AsyncHttpClient httpClient, String apiKey, String secret) {
		AsyncHttpClientCallFactory.AsyncHttpClientCallFactoryBuilder callFactoryBuilder =
				AsyncHttpClientCallFactory.builder().httpClient(httpClient);
		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
				.baseUrl(BinanceApiConstants.API_BASE_URL)
				.addConverterFactory(converterFactory)
				.validateEagerly(true);
		BinanceCallCustomizer.customize(apiKey, secret, callFactoryBuilder);
		Retrofit retrofit = retrofitBuilder.callFactory(callFactoryBuilder.build()).build();
		return retrofit.create(serviceClass);
	}

	/**
	 * Execute a REST call and block until the response is received.
	 *
	 * @param call the REST call to execute
	 * @param <T>  the response type
	 *
	 * @return the response
	 */
	public static <T> T executeSync(Call<T> call) {
		try {
			Response<T> response = call.execute();
			if (response.isSuccessful()) {
				return response.body();
			} else {
				BinanceApiError apiError = getBinanceApiError(response);
				throw new BinanceApiException(apiError);
			}
		} catch (IOException e) {
			throw new BinanceApiException(e);
		}
	}

	/**
	 * Extracts and converts the response error body into an object.
	 *
	 * @param response the raw error response
	 *
	 * @return the error body
	 *
	 * @throws IOException
	 * @throws BinanceApiException
	 */
	public static BinanceApiError getBinanceApiError(Response<?> response) throws IOException, BinanceApiException {
		return errorBodyConverter.convert(response.errorBody());
	}
}