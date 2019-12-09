package com.univocity.trader.exchange.binance.api.client.security;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import okhttp3.*;
import okio.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.util.*;

/**
 * A request interceptor that injects the API Key Header into requests, and signs messages, whenever required.
 */
public class AuthenticationInterceptor implements Interceptor {

	private final String apiKey;

	private final String secret;

	public AuthenticationInterceptor(String apiKey, String secret) {
		this.apiKey = apiKey;
		this.secret = secret;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request original = chain.request();
		Request.Builder newRequestBuilder = original.newBuilder();

		boolean isApiKeyRequired = original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY) != null;
		boolean isSignatureRequired = original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED) != null;
		newRequestBuilder.removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY)
				.removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED);

		// Endpoint requires sending a valid API-KEY
		if (isApiKeyRequired || isSignatureRequired) {
			newRequestBuilder.addHeader(BinanceApiConstants.API_KEY_HEADER, apiKey);
		}

		// Endpoint requires signing the payload
		if (isSignatureRequired) {
			String payload = original.url().query();
			if (!StringUtils.isEmpty(payload)) {
				String signature = HmacSHA256Signer.sign(payload, secret);
				HttpUrl signedUrl = original.url().newBuilder().addQueryParameter("signature", signature).build();
				newRequestBuilder.url(signedUrl);
			}
		}

		// Build new request after adding the necessary authentication information
		Request newRequest = newRequestBuilder.build();
		return chain.proceed(newRequest);
	}

	/**
	 * Extracts the request body into a String.
	 *
	 * @return request body as a string
	 */
	@SuppressWarnings("unused")
	private static String bodyToString(RequestBody request) {
		try (final Buffer buffer = new Buffer()) {
			final RequestBody copy = request;
			if (copy != null) {
				copy.writeTo(buffer);
			} else {
				return "";
			}
			return buffer.readUtf8();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AuthenticationInterceptor that = (AuthenticationInterceptor) o;
		return Objects.equals(apiKey, that.apiKey) &&
				Objects.equals(secret, that.secret);
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiKey, secret);
	}
}