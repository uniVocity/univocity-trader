package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IchimokuIndicatorTest {

    private List<Candle> candles;

    @Before
    public void setUp() {
        int i = 0;
        candles = new ArrayList<>();
        candles.add(CandleHelper.newCandle(i++, 44.98, 45.05, 45.17, 44.96));
        candles.add(CandleHelper.newCandle(i++, 45.05, 45.10, 45.15, 44.99));
        candles.add(CandleHelper.newCandle(i++, 45.11, 45.19, 45.32, 45.11));
        candles.add(CandleHelper.newCandle(i++, 45.19, 45.14, 45.25, 45.04));
        candles.add(CandleHelper.newCandle(i++, 45.12, 45.15, 45.20, 45.10));
        candles.add(CandleHelper.newCandle(i++, 45.15, 45.14, 45.20, 45.10));
        candles.add(CandleHelper.newCandle(i++, 45.13, 45.10, 45.16, 45.07));
        candles.add(CandleHelper.newCandle(i++, 45.12, 45.15, 45.22, 45.10));
        candles.add(CandleHelper.newCandle(i++, 45.15, 45.22, 45.27, 45.14));
        candles.add(CandleHelper.newCandle(i++, 45.24, 45.43, 45.45, 45.20));
        candles.add(CandleHelper.newCandle(i++, 45.43, 45.44, 45.50, 45.39));
        candles.add(CandleHelper.newCandle(i++, 45.43, 45.55, 45.60, 45.35));
        candles.add(CandleHelper.newCandle(i++, 45.58, 45.55, 45.61, 45.39));
        candles.add(CandleHelper.newCandle(i++, 45.45, 45.01, 45.55, 44.80));
        candles.add(CandleHelper.newCandle(i++, 45.03, 44.23, 45.04, 44.17));
        candles.add(CandleHelper.newCandle(i++, 44.23, 43.95, 44.29, 43.81));
        candles.add(CandleHelper.newCandle(i++, 43.91, 43.08, 43.99, 43.08));
        candles.add(CandleHelper.newCandle(i++, 43.07, 43.55, 43.65, 43.06));
        candles.add(CandleHelper.newCandle(i++, 43.56, 43.95, 43.99, 43.53));
        candles.add(CandleHelper.newCandle(i++, 43.93, 44.47, 44.58, 43.93));
    }

    @Test
    public void test1() {
        TimeInterval interval = TimeInterval.MINUTE;

        IchimokuTenkanSen tenkanSen = new IchimokuTenkanSen(3, interval);
        IchimokuKijunSen kijunSen = new IchimokuKijunSen(5, interval);
        IchimokuSenkouSpanB senkouSpanB = new IchimokuSenkouSpanB(9, interval);

        // TenkanSen

        tenkanSen.accumulate(candles.get(0));
        tenkanSen.accumulate(candles.get(1));
        tenkanSen.accumulate(candles.get(2));
        tenkanSen.accumulate(candles.get(3));
        assertEquals(45.155, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(4));
        assertEquals(45.18, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(5));
        assertEquals(45.145, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(6));
        assertEquals(45.135, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(7));
        assertEquals(45.145, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(8));
        assertEquals(45.17, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(9));
        tenkanSen.accumulate(candles.get(10));
        tenkanSen.accumulate(candles.get(11));
        tenkanSen.accumulate(candles.get(12));
        tenkanSen.accumulate(candles.get(13));
        tenkanSen.accumulate(candles.get(14));
        tenkanSen.accumulate(candles.get(15));
        tenkanSen.accumulate(candles.get(16));
        assertEquals(44.06, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(17));
        assertEquals(43.675, tenkanSen.getValue(), 0.0001);
        tenkanSen.accumulate(candles.get(18));
        assertEquals(43.525, tenkanSen.getValue(), 0.0001);

        // KijunSen

        kijunSen.accumulate(candles.get(0));
        kijunSen.accumulate(candles.get(1));
        kijunSen.accumulate(candles.get(2));
        kijunSen.accumulate(candles.get(3));
        assertEquals(45.14, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(4));
        assertEquals(45.14, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(5));
        assertEquals(45.155, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(6));
        assertEquals(45.18, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(7));
        assertEquals(45.145, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(8));
        assertEquals(45.17, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(9));
        kijunSen.accumulate(candles.get(10));
        kijunSen.accumulate(candles.get(11));
        kijunSen.accumulate(candles.get(12));
        kijunSen.accumulate(candles.get(13));
        kijunSen.accumulate(candles.get(14));
        kijunSen.accumulate(candles.get(15));
        kijunSen.accumulate(candles.get(16));
        assertEquals(44.345, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(17));
        assertEquals(44.305, kijunSen.getValue(), 0.0001);
        kijunSen.accumulate(candles.get(18));
        assertEquals(44.05, kijunSen.getValue(), 0.0001);

        // SenkouSpanB

        senkouSpanB.accumulate(candles.get(0));
        senkouSpanB.accumulate(candles.get(1));
        senkouSpanB.accumulate(candles.get(2));
        senkouSpanB.accumulate(candles.get(3));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(4));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(5));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(6));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(7));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(8));
        assertEquals(45.14, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(9));
        senkouSpanB.accumulate(candles.get(10));
        senkouSpanB.accumulate(candles.get(11));
        senkouSpanB.accumulate(candles.get(12));
        senkouSpanB.accumulate(candles.get(13));
        senkouSpanB.accumulate(candles.get(14));
        senkouSpanB.accumulate(candles.get(15));
        senkouSpanB.accumulate(candles.get(16));
        assertEquals(44.345, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(17));
        assertEquals(44.335, senkouSpanB.getValue(), 0.0001);
        senkouSpanB.accumulate(candles.get(18));
        assertEquals(44.335, senkouSpanB.getValue(), 0.0001);

    }

    @Test
    public void test2() {

        TimeInterval interval = TimeInterval.MINUTE;

        IchimokuTenkanSen tenkanSen = new IchimokuTenkanSen(3, interval);
        IchimokuKijunSen kijunSen = new IchimokuKijunSen(5, interval);
        IchimokuSenkouSpanA senkouSpanA = new IchimokuSenkouSpanA(interval, tenkanSen, kijunSen);

        // SenkouSpanA

        senkouSpanA.accumulate(candles.get(0));
        senkouSpanA.accumulate(candles.get(1));
        senkouSpanA.accumulate(candles.get(2));
        senkouSpanA.accumulate(candles.get(3));
        assertEquals(45.1475, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(4));
        assertEquals(45.16, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(5));
        assertEquals(45.15, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(6));
        assertEquals(45.1575, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(7));
        assertEquals(45.145, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(8));
        assertEquals(45.17, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(9));
        senkouSpanA.accumulate(candles.get(10));
        senkouSpanA.accumulate(candles.get(11));
        senkouSpanA.accumulate(candles.get(12));
        senkouSpanA.accumulate(candles.get(13));
        senkouSpanA.accumulate(candles.get(14));
        senkouSpanA.accumulate(candles.get(15));
        senkouSpanA.accumulate(candles.get(16));
        assertEquals(44.2025, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(17));
        assertEquals(43.99, senkouSpanA.getValue(), 0.0001);
        senkouSpanA.accumulate(candles.get(18));
        assertEquals(43.7875, senkouSpanA.getValue(), 0.0001);

    }

}
