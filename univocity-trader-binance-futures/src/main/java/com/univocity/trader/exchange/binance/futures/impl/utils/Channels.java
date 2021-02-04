package com.univocity.trader.exchange.binance.futures.impl.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.univocity.trader.exchange.binance.futures.model.enums.CandlestickInterval;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Channels {

    public static final String OP_SUB = "sub";
    public static final String OP_REQ = "req";

    public static String aggregateTradeChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@aggTrade");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String markPriceChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@markPrice");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String candlestickChannel(String symbols, CandlestickInterval interval) {
        /*
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@kline_%s", s, interval))
                .collect(Collectors.joining("/"));*/
        final String[] channels = symbols.split(",");

        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        for(String channel : channels) {
            params.add(channel + "@kline_" + interval);
        }
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String miniTickerChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@miniTicker");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String miniTickerChannel() {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add("!miniTicker@arr");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String tickerChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@ticker");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String tickerChannel() {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add("!ticker@arr");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String bookTickerChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@bookTicker");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String bookTickerChannel() {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add("!bookTicker");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String liquidationOrderChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@forceOrder");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String liquidationOrderChannel() {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add("!forceOrder@arr");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String bookDepthChannel(String symbol, Integer limit) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@depth" + limit);
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String diffDepthChannel(String symbol) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(symbol + "@depth");
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
    public static String userDataChannel(String listenKey) {
        JSONObject json = new JSONObject();
        JSONArray params = new JSONArray();
        params.add(listenKey);
        json.put("params", params);
        json.put("id", System.currentTimeMillis());
        json.put("method", "SUBSCRIBE");
        return json.toJSONString();
    }
  
}