package com.univocity.trader.vendor.iqfeed.api.client;

public class IQFeedApiError {

    private int code;
    private String msg;

    @Override
    public String toString(){
        return new ToStringBuilder(this, IQFeedApiConstants.TO_STRING_BUILDER_STYE)
                .append("code", code)
                .append("msg", msg)
                .toString();
    }

}
