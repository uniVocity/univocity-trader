package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;


public class WaddahAttarExplosion {


    private BollingerBand BB ;
    private MACD macd;
    public long sensitivity , BBmultiplier;
    public int deadZone , slowLength , fastLength , BBchannelLength;
    public TimeInterval interval;
    public boolean upTrend;

    public WaddahAttarExplosion( long sensitivity , int deadZone , int fastLength , int slowLength , int BBchannelLength , long BBmultiplier , TimeInterval interval , ToDoubleFunction<Candle> valueGetter ) {

        BB = new BollingerBand( BBchannelLength , interval  , valueGetter);
		macd = new MACD( fastLength , slowLength , 9 , valueGetter);
        this.sensitivity = sensitivity;
        this.deadZone = deadZone;
        this.BBmultiplier = BBmultiplier;

	}


    public calculate(Candle candle, double value, boolean updating){

        BB.calculateIndicatorValue(Candle candle, double value, boolean updating);
        macd.process(Candle candle, double value, boolean updating);
        double t1 =
        double e1 = 
    }


    public double getBBupper() {
        
    }


    public double getBBlower() {

    }













}
