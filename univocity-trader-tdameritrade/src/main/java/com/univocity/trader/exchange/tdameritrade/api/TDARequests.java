package com.univocity.trader.exchange.tdameritrade.api;

import org.slf4j.LoggerFactory;

//abstract class TDARequests {
//
//    private static final logger log = LoggerFactory.getLogger(TDARequests.class);
//
//    protected EClientSocket client;
//    private EJavaSignal signal;
//    private EReader reader;
//
//    final RequestHandler requestHandler;
//    final ResponseProcessor responseProcessor;
//
//    final String ip;
//    final int port;
//    final int clientID;
//    final String optionalCapabilities;
//    private boolean reconnecting = false;int clientID;
//
//    public TDARequests(String ip, int port, int clientID, String optionalCapabilities, Runnable reconnectionProcess) {
//        this.clientID = clientID;
//        this.ip = ip;
//        this.optionalCapabilities = optionalCapabilities;
//        this.port = port;
//
//        this.requestHandler = requestHandler;
//        this.responseProcessor = responseProcessor;
//    }
//
//    abstract TDARequests newInstance(TDARequests oldInstance);
//
//}
