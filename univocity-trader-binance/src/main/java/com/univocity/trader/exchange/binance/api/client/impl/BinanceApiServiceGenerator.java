package com.univocity.trader.exchange.binance.api.client.impl;


import com.univocity.trader.exchange.binance.api.client.BinanceApiError;
import com.univocity.trader.exchange.binance.api.client.config.BinanceApiConfig;
import com.univocity.trader.exchange.binance.api.client.exception.BinanceApiException;
import okhttp3.ResponseBody;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.extras.retrofit.AsyncHttpClientCallFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Generates a Binance API implementation based on @see {@link BinanceApiService}.
 */
public class BinanceApiServiceGenerator {
	private static final Logger log = LoggerFactory.getLogger(BinanceApiServiceGenerator.class);

	private static final Converter.Factory converterFactory = JacksonConverterFactory.create();

	@SuppressWarnings("unchecked")
	private static final Converter<ResponseBody, BinanceApiError> errorBodyConverter =
			(Converter<ResponseBody, BinanceApiError>) converterFactory.responseBodyConverter(
					BinanceApiError.class, new Annotation[0], null);
	public static final int MAX_RETRY_FAILURES = 5;

	public static <S> S createService(Class<S> serviceClass, AsyncHttpClient httpClient) {
		return createService(serviceClass, new BinanceApiConfig(null, null, httpClient, false));
	}

	public static <S> S createService(Class<S> serviceClass, AsyncHttpClient httpClient, String apiKey, String secret) {
		return createService(serviceClass, new BinanceApiConfig(apiKey, secret, httpClient,  false));
    }

	public static <S> S createService(Class<S> serviceClass, BinanceApiConfig apiConfig) {
		AsyncHttpClientCallFactory.AsyncHttpClientCallFactoryBuilder callFactoryBuilder =
				AsyncHttpClientCallFactory.builder().httpClient(apiConfig.getAsyncHttpClient());
		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
				.baseUrl(apiConfig.getApiBaseUrl())
				.addConverterFactory(converterFactory)
				.validateEagerly(true);
		BinanceCallCustomizer.customize(apiConfig.getApiKey(), apiConfig.getSecret(), callFactoryBuilder);
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
		boolean success = false;
		int failures=0;
		while (!success && failures < MAX_RETRY_FAILURES) {
			try {
				Response<T> response = call.clone().execute();
				if (response.isSuccessful()) {
					success = true;
					if (failures > 0) {
						log.trace("Successful call with failures count set to " + failures);
					}
					return response.body();
				} else {
					success = true;
					BinanceApiError apiError = getBinanceApiError(response);
					throw new BinanceApiException(apiError);
				}
			} catch (IOException e) {
				log.error("Got IO Exception. Will retry in 5 seconds. Original error: "+e.getMessage(), e);
				failures++;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException interruptedException) {
					log.error("Received interrupt", interruptedException);
				}
			}
		}

		return null;
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