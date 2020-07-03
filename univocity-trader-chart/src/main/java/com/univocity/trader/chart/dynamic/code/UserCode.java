package com.univocity.trader.chart.dynamic.code;

import com.univocity.trader.candles.*;

import java.io.*;
import java.util.function.*;

public class UserCode {

	public static void main(String... args) throws Exception {
		MemoryJavaCompiler compiler = new MemoryJavaCompiler();

		final String source = "" +
				"import com.univocity.trader.candles.*;" +
				"import java.util.function.*;" +
				"public final class Custom implements ToDoubleFunction<Candle> {" +
				" public double applyAsDouble(Candle candle) {" +
				"   return (candle.high + candle.low) / 2.0;" +
				" }" +
				"}";

		ToDoubleFunction<Candle> f = compiler.compileString("Custom", source, new PrintWriter(System.err));
		double result = f.applyAsDouble(new Candle(10, 10, 10.0, 20.0, 10.0, 10.0, 10.0));
		System.out.println(result);

	}
}
