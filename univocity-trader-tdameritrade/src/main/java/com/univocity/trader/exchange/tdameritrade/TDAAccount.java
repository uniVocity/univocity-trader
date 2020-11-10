package com.univocity.trader.exchange.tdameritrade;

import com.univocity.trader.ClientAccount;
import com.univocity.trader.account.Balance;
import com.univocity.trader.account.Order;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;

import java.util.concurrent.ConcurrentHashMap;

//public class TDAAccount implements ClientAccount {
//
//    private final TDA tda;
//    private final Account account;
//
//    private final ConcurrentHashMap<String, Balance> balances = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
//
//    public TDAAccount(TDA tda, Account account) {
//        this.tda = tda;
//        this.account = account;
//        tda.getAccountBalances(account.referenceCurrency(), balances);
//    }
//
//    @Override
//    public Order executeOrder(OrderRequest orderDetails){
//        return null;}
//
//    @Override
//    public ConcurrentHashMap <String, Balance> updateBalances(boolean force){ return balances;}
//
//    @Override
//    public OrderBook getOrderBook(String symbol, int depth){
//        return null;
//    }
//
//    @Override
//    public Order updateOrderStatus(Order order){
//        return null;
//    }
//
//    @Override
//    public void cancel(Order order){
//    }
//}
