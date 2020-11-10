package com.univocity.trader.exchange.tdameritrade.constant;

import org.apache.commons.lang3.builder.ToStringStyle;

public class TDAmeritradeApiConstants {

    public static final String API_BASE_URL = "https://api.tdameritrade.com";

    /**
     * Default receiving window.
     */
    public static final long DEFAULT_RECEIVING_WINDOW = 60_000L;

    /**
     * Default ToStringStyle used by toString methods. Override this to change the
     * output format of the overridden toString methods. - Example
     * ToStringStyle.JSON_STYLE
     */
    public static ToStringStyle TO_STRING_BUILDER_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;
}
