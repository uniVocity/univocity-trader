package com.univocity.trader.exchange.tdameritrade.model.Account;

import java.text.MessageFormat;

public class GetAccounts {
    String resourceURL = "https://api.tdameritrade.com/v1/accounts/{0}/orders/{1}";
    public String GetAccounts(String accountId, String orderId){
        this.resourceURL = MessageFormat.format(resourceURL, accountId, orderId);
        return this.resourceURL;
    }
}
