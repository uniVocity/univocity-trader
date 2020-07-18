package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;


public class WaddahAttarExplosion extends SingleValueIndicator {


    private BollingerBand BB ;
    private MACD macd;
    private double sensitivity , BBmultiplier ,explosion,trend , newMACDvalue , oldMACDvalue;
    private int  slowLength , fastLength , BBchannelLength;
    private TimeInterval interval;
    private boolean upTrend;
    private boolean firstProcess=true;

    public WaddahAttarExplosion( double sensitivity , int fastLength , int slowLength , int BBchannelLength , double BBmultiplier , TimeInterval interval , ToDoubleFunction<Candle> valueGetter ) {

        BB = new BollingerBand( BBchannelLength , interval  , valueGetter);
		macd = new MACD( fastLength , slowLength , 9 , valueGetter);
        this.sensitivity = sensitivity;
        this.BBmultiplier = BBmultiplier;

	}

    private void addNewMACD(double newValue){
        oldMACDvalue = newMACDvalue;
        newMACDvalue = newValue ;
    }

    public boolean process(Candle candle, double value, boolean updating){
        

        BB.calculateIndicatorValue(Candle candle, double value, boolean updating);
        macd.process(Candle candle, double value, boolean updating);
        addNewMACD(macd.getValue() );

        if (firstProcess){
            trend =0;
            firstProcess=false;
        }
        else{
            trend = ( newMACDvalue - oldMACDvalue )*sensitivity;
        }

        trendUp = (trend >= 0) ;
        if (!trendUp)
            trend *= -1;
        
        explosion = BB.getUpperBand - BB.getLowerBand ;


		return true;
    }



    public double getTrend(){
        return trend;
    }


    public double getExplosion(){
        return explosion;
    }


    public boolean isTrendUp(){
        return trendUp;
    }




}
