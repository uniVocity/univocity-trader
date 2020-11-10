//package com.univocity.trader.exchange.tdameritrade.api;
//
//public class TDAmeritradeApi extends TDARequests {
//
//    public TDAmeritradeApi(String ip, int port, int clientID, String optionalCapabilities, Runnable reconnectionProcess) {
//        super(ip, port, clientID, optionalCapabilities, reconnectionProcess);
//    }
//
//    @Override
//    TDARequests newInstance(TDARequests old) {
//        return new TDAmeritradeApi(old.ip, old.port, old.clientID, old.optionalCapabilities, old.requestHandler.reconnectProcess);
//    }
//}
