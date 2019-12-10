package com.univocity.trader.vendor.iqfeed.api.client.domain.request;

//@author jecker

import com.univocity.trader.indicators.base.TimeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

public final class IQFeedHistoricalRequestBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO: add in validation for individual data types
    // request strings for IQFeed interface
    // In general: timeframes are specified in the following format: CCYYMMDD HHmmSS
    // see here http://www.iqfeed.net/dev/api/docs/HistoricalviaTCPIP.cfm for more information
    // tick / max pts
    // required
    protected String symbol = "";
    protected String tickSize= "";
    // optional
    protected String beginDate= "";
    protected Long beginDateTime;
    protected String beginFilterTime= "";
    protected String dataDirection= "";
    protected String dataPtsPerSend= "";
    protected String endDate= "";
    protected Long endDateTime;
    protected String endFilterTime= "";
    protected String includePartialData= "";
    protected String interval= "";
    protected TimeInterval intervalType;
    protected String svtIntervalType;
    protected String labelAtBeginning= "";
    protected String maxDataPts= "";
    protected String maxDays= "";
    protected String maxMonths= "";
    protected String maxWeeks= "";
    protected String requestID= "";

    public IQFeedHistoricalRequestBuilder() {
    }

    public static IQFeedHistoricalRequestBuilder anIQFeedHistoricalRequest() {
        return new IQFeedHistoricalRequestBuilder();
    }

    public IQFeedHistoricalRequestBuilder setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setDataType(String dataType) {
        this.tickSize = dataType;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setBeginDate(String beginDate) {
        this.beginDate = beginDate;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setBeginDateTime(Long beginDateTime) {
        this.beginDateTime = beginDateTime;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setBeginFilterTime(String beginFilterTime) {
        this.beginFilterTime = beginFilterTime;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setDataDirection(String dataDirection) {
        this.dataDirection = dataDirection;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setDataPtsPerSend(String dataPtsPerSend) {
        this.dataPtsPerSend = dataPtsPerSend;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setEndDateTime(Long endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setEndFilterTime(String endFilterTime) {
        this.endFilterTime = endFilterTime;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setIncludePartialData(String includePartialData) {
        this.includePartialData = includePartialData;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setInterval(String interval) {
        this.interval = interval;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setIntervalType(TimeInterval intervalType) {
        this.intervalType = intervalType;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setSvtIntervalType(String svtIntervalType) {
        this.svtIntervalType = svtIntervalType;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setLabelAtBeginning(String labelAtBeginning) {
        this.labelAtBeginning = labelAtBeginning;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setMaxDataPts(String maxDataPts) {
        this.maxDataPts = maxDataPts;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setMaxDays(String maxDays) {
        this.maxDays = maxDays;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setMaxMonths(String maxMonths) {
        this.maxMonths = maxMonths;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setMaxWeeks(String maxWeeks) {
        this.maxWeeks = maxWeeks;
        return this;
    }

    public IQFeedHistoricalRequestBuilder setRequestID(String requestID) {
        this.requestID = requestID;
        return this;
    }

    public IQFeedHistoricalRequestBuilder but() {
        return anIQFeedHistoricalRequest().setSymbol(symbol).setDataType(tickSize).setBeginDate(beginDate).setBeginDateTime(beginDateTime).setBeginFilterTime(beginFilterTime).setDataDirection(dataDirection).setDataPtsPerSend(dataPtsPerSend).setEndDate(endDate).setEndDateTime(endDateTime).setEndFilterTime(endFilterTime).setIncludePartialData(includePartialData).setInterval(interval).setIntervalType(intervalType).setSvtIntervalType(svtIntervalType).setLabelAtBeginning(labelAtBeginning).setMaxDataPts(maxDataPts).setMaxDays(maxDays).setMaxMonths(maxMonths).setMaxWeeks(maxWeeks).setRequestID(requestID);
    }



    private boolean validate(IQFeedHistoricalRequest request) throws Exception{
        // TODO: refactor to check for invalid parameter values, redundant/extra fields
        /*
          in general, we need the following combinations for a valid request, dependent on ticksize:
                tick: maxDataPoints || maxDays || (BeginDateTime, EndDateTime)
                interval: maxDataPoints || maxDays || (BeginFilterTime, EndFilterTime)
                day: maxDays || (beginDate, endDate)
                week: maxWeeks
                month: maxMonth
         */
        try {
            if(!checkVal(request.symbol)){
                throw new InvalidParameterException("Invalid parameter for SYMBOL");
            }
            if(!checkVal(request.dataPeriod)){
                throw new InvalidParameterException("Invalid parameter for DATAPERIOD");
            }
            String size = request.dataPeriod.toLowerCase();
            switch(size){
                case "tick":
                    if(!(checkVal(request.maxDataPts) || checkVal(request.maxDays) ||
                            (checkVal(request.beginDateTime.toString()) && checkVal(request.endDateTime.toString())))){
                        throw new InvalidParameterException("Invalid parameters for request of tick size: tick");
                    }
                    break;
                case "minute":
                    if(!(checkVal(request.maxDataPts) || checkVal(request.maxDays) ||
                            (checkVal(request.beginFilterTime) && checkVal(request.endFilterTime)))){
                        throw new InvalidParameterException("Invalid parameters for request of tick size: interval");
                    }
                    break;
                case "day":
                    if(!(checkVal(request.maxDays) || (checkVal(request.beginDate) && checkVal(request.beginDate)))){
                        throw new InvalidParameterException("Invalid parameters for request of tick size: day");
                    }
                    break;
                case "interval":
                    if( !checkVal(request.interval) || !(checkVal(request.maxDataPts) || checkVal(request.maxDays) ||
                            (checkVal(request.beginFilterTime) && checkVal(request.endFilterTime)))){
                        throw new InvalidParameterException("Invalid parameters for request of tick size: interval");
                    }
                    break;
                case "week":
                    if(!checkVal(request.maxWeeks)){
                        throw new InvalidParameterException("Invalid parameters for request of tick size: week");
                    }
                    break;
                case "month":
                    if(!checkVal(request.maxMonths)) {
                        throw new InvalidParameterException("Invalid parameters for request of tick size: month");
                    }
                    break;
            }
            return true;
        } catch(Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private boolean checkVal(String val) {
        if(val == "" || val == null){
            return false;
        }
        return true;
    }

    public IQFeedHistoricalRequest build() {
        IQFeedHistoricalRequest request = new IQFeedHistoricalRequest(this);
        IQFeedHistoricalRequest validatedRequest = null;
        try {
            if(validate(request)){
                validatedRequest = request;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return validatedRequest;
    }
}
