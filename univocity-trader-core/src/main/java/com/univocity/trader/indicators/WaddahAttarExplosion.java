package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;


public class WaddahAttarExplosion extends SingleValueIndicator {


    private BollingerBand BB ;
    private MACD macd;
    private double sensitivity ,explosion,trend , newMACDvalue , oldMACDvalue;
    private boolean upTrend;
    private boolean firstProcess=true;

    public WaddahAttarExplosion( double sensitivity , int fastLength , int slowLength , int BBchannelLength , double BBmultiplier , 
    																								TimeInterval interval , ToDoubleFunction<Candle> valueGetter ) {
    	super(interval, null);
        BB = new BollingerBand( BBchannelLength ,BBmultiplier, interval  , valueGetter);
		macd = new MACD( fastLength , slowLength , 9 ,interval, valueGetter);
        this.sensitivity = sensitivity;
	}
    
    public WaddahAttarExplosion( double sensitivity , int fastLength , int slowLength , int BBchannelLength , double BBmultiplier , TimeInterval interval ) {
    	this(  sensitivity , fastLength , slowLength , BBchannelLength, BBmultiplier ,  interval ,c -> c.close);

	}

    private void addNewMACD(double newValue){
        oldMACDvalue = newMACDvalue;
        newMACDvalue = newValue ;
    }

    public boolean process(Candle candle, double value, boolean updating){
        
    	
        BB.calculateIndicatorValue( candle,  value,  updating);
        macd.process( candle,  value,  updating);
        addNewMACD(macd.getValue() );

        if (firstProcess){
            trend =0;
            firstProcess=false;
        }
        else{
            trend = ( newMACDvalue - oldMACDvalue )*sensitivity;
        }

        upTrend = (trend >= 0) ;
        if (!upTrend)
            trend *= -1;
        
        explosion = BB.getUpperBand() - BB.getLowerBand() ;


		return true;
    }



    public double getTrend(){
        return trend;
    }


    public double getExplosion(){
        return explosion;
    }


    public boolean isTrendUp(){
        return upTrend;
    }

	@Override
	protected Indicator[] children() {
		return new Indicator[]{BB, macd};
	}


}
