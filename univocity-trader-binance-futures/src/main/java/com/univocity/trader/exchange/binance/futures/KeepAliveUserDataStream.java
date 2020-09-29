package com.univocity.trader.exchange.binance.futures;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Maintain the data stream alive.
 */
public class KeepAliveUserDataStream {

    SyncRequestClient client;
    private String listenKey;
    private Timer timer;

    public KeepAliveUserDataStream(SyncRequestClient client) {
        this.client = client;
    }

    public void start() {
        this.listenKey = client.startUserDataStream();
        TimerTask task = new TimerTask() {
            public void run() {
//                client.ping();
//                client.keepAliveUserDataStream(listenKey);
            }
        };
        timer = new Timer("Keep-alive Timer", true);
        long delay = 30000L; // this timeout is as recommended by Binance
        timer.scheduleAtFixedRate(task, delay, delay);
    }
}
