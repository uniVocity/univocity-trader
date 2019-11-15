package com.univocity.trader;

import com.univocity.trader.account.*;

public interface TradingFees {

	double takeFee(double amount, Order.Type orderType, Order.Side side);

	default double getBreakEvenAmount(double amount) {
		double out = takeFee(amount, Order.Type.LIMIT, Order.Side.BUY);
		out = takeFee(out, Order.Type.LIMIT, Order.Side.BUY);
		return amount + (amount - out);
	}

	default double getBreakEvenChange(double amount) {
		if(amount == 0.0){
			return 0.0;
		}
		double breakEvenAmount = getBreakEvenAmount(amount);
		return ((breakEvenAmount / amount) - 1) * 100.0;
	}

}
