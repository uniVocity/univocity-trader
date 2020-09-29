package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.indicators.base.*;

/**
 * 1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
 */
public enum CandlestickInterval {
    ONE_MINUTE("1m"),
    THREE_MINUTES("3m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    HALF_HOURLY("30m"),
    HOURLY("1h"),
    TWO_HOURLY("2h"),
    FOUR_HOURLY("4h"),
    SIX_HOURLY("6h"),
    EIGHT_HOURLY("8h"),
    TWELVE_HOURLY("12h"),
    DAILY("1d"),
    THREE_DAILY("3d"),
    WEEKLY("1w"),
    MONTHLY("1M");

    private final String code;

    CandlestickInterval(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static CandlestickInterval fromTimeInterval(TimeInterval tickInterval) {
        switch (tickInterval.unit) {
            case MINUTES:
                switch ((int) tickInterval.duration) {
                    case 1:
                        return ONE_MINUTE;
                    case 3:
                        return THREE_MINUTES;
                    case 5:
                        return FIVE_MINUTES;
                    case 15:
                        return FIFTEEN_MINUTES;
                    case 30:
                        return HALF_HOURLY;
                    case 60:
                        return HOURLY;
                }
                break;
            case HOURS:
                switch ((int) tickInterval.duration) {
                    case 1:
                        return HOURLY;
                    case 2:
                        return TWO_HOURLY;
                    case 3:
                        return FOUR_HOURLY;
                    case 6:
                        return SIX_HOURLY;
                    case 8:
                        return EIGHT_HOURLY;
                    case 12:
                        return TWELVE_HOURLY;
                    case 24:
                        return DAILY;
                }
                break;
            case DAYS:
                switch ((int) tickInterval.duration) {
                    case 1:
                        return DAILY;
                    case 3:
                        return THREE_DAILY;
                    case 7:
                        return WEEKLY;
                    case 30:
                        return MONTHLY;
                }
                break;
        }
        throw new IllegalArgumentException("Unsupported time interval: " + tickInterval + ".");
    }
}
