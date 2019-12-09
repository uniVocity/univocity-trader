package com.univocity.trader.exchange.binance.api.client.security;

import org.apache.commons.codec.binary.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Utility class to sign messages using HMAC-SHA256.
 */
public class HmacSHA256Signer {

	/**
	 * Sign the given message using the given secret.
	 *
	 * @param message message to sign
	 * @param secret  secret key
	 * @return a signed message
	 */
	public static String sign(String message, String secret) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secretKeySpec);
			return new String(Hex.encodeHex(sha256_HMAC.doFinal(message.getBytes())));
		} catch (Exception e) {
			throw new RuntimeException("Unable to sign message.", e);
		}
	}
}
