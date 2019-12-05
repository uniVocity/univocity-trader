package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;

import java.util.List;

public class IQFeedProcessor {

    public List<Candlestick> process(String payload, String type){
        switch(type.toLowerCase()){
            case "historical":
                return processHistoricalResponse(payload);
        }
    }

    public List<Candlestick> processHistoricalResponse(String payload){

    }
}
