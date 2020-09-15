package com.univocity.trader.exchange.binance.futures.model.user;

import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class AccountUpdate {

    private List<BalanceUpdate> balances;

    private List<PositionUpdate> positions;

    public List<BalanceUpdate> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceUpdate> balances) {
        this.balances = balances;
    }

    public List<PositionUpdate> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionUpdate> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("balances", balances)
                .append("positions", positions).toString();
    }
}
