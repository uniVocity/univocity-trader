package com.univocity.trader.vendor.iqfeed.api.client.exception;

import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiError;

public class IQFeedApiException extends RuntimeException{
    private IQFeedApiError error;

    public IQFeedApiException(IQFeedApiError error) { this.error = error;}

    public IQFeedApiException(){ super();}
}

