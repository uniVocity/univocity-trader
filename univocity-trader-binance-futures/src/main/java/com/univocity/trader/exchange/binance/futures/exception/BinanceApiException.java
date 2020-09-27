package com.univocity.trader.exchange.binance.futures.exception;

public class BinanceApiException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 4360108982268949009L;
    public static final String RUNTIME_ERROR = "RuntimeError";
    public static final String INPUT_ERROR = "InputError";
    public static final String KEY_MISSING = "KeyMissing";
    public static final String SYS_ERROR = "SysError";
    public static final String SUBSCRIPTION_ERROR = "SubscriptionError";
    public static final String ENV_ERROR = "EnvironmentError";
    public static final String EXEC_ERROR = "ExecuteError";
    private final String errCode;

    public BinanceApiException(String errType, String errMsg) {
        super(errMsg);
        this.errCode = errType;
    }

    public BinanceApiException(String errType, String errMsg, Throwable e) {
        super(errMsg, e);
        this.errCode = errType;
    }

    public String getErrType() {
        return this.errCode;
    }
}
