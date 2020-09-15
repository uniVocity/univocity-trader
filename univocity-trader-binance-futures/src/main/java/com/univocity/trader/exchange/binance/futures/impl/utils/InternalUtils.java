package com.univocity.trader.exchange.binance.futures.impl.utils;

import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

public abstract class InternalUtils {

  public static byte[] decode(byte[] data) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    decompress(bais, baos);
    baos.flush();
    baos.close();
    bais.close();
    return baos.toByteArray();
  }

  private static void decompress(InputStream is, OutputStream os) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(is);
    int count;
    byte[] data = new byte[1024];
    while ((count = gis.read(data, 0, 1024)) != -1) {
      os.write(data, 0, count);
    }
    gis.close();
  }

  public static void await(long n) throws BinanceApiException {
    try {
      Thread.sleep(n);
    } catch (InterruptedException e) {
      throw new BinanceApiException(BinanceApiException.SYS_ERROR, "Error when sleep", e);
    }
  }
}
