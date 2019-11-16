package com.univocity.trader.requests;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class IQFeedHistoricalRequest {

    // TODO: refactor,  add validation

    // request strings for IQFeed interface
    // In general: timeframes are specified in the following format: CCYYMMDD HHmmSS
    // see here http://www.iqfeed.net/dev/api/docs/HistoricalviaTCPIP.cfm for more information
    // tick / max pts
    // required
    public String symbol;
    public String dataPeriod;
    // optional
    public String beginDate;
    public String beginDateTime;
    public String beginFilterTime;
    public String dataDirection;
    public String dataPtsPerSend;
    public String endDate;
    public String endDateTime;
    public String endFilterTime;
    public String includePartialData;
    public String interval;
    public String intervalType;
    public String labelAtBeginning;
    public String maxDataPts;
    public String maxDays;
    public String maxMonths;
    public String maxWeeks;
    public String requestID;


    public IQFeedHistoricalRequest(IQFeedHistoricalRequestBuilder builder) {
        symbol = builder.symbol;
        dataPeriod = builder.tickSize;
        beginDate = builder.beginDate;
        beginDateTime = builder.beginDateTime;
        beginFilterTime = builder.beginFilterTime;
        dataDirection = builder.dataDirection;
        dataPtsPerSend = builder.dataPtsPerSend;
        endDate = builder.endDate;
        endDateTime = builder.endDateTime;
        endFilterTime = builder.endFilterTime;
        includePartialData = builder.includePartialData;
        interval= builder.interval;
        intervalType = builder.intervalType;
        labelAtBeginning = builder.labelAtBeginning;
        maxDataPts = builder.maxDataPts;
        maxDays = builder.maxDays;
        maxMonths = builder.maxMonths;
        maxWeeks = builder.maxWeeks;
        requestID = builder.requestID;
       // validating the requests here
    }

    public String toString() throws InvalidParameterException {
        String dataQualifier = null;
        String timeQualifier = null;

        switch (dataPeriod.toLowerCase()) {
            // TODO: convert strings to enums
            case "tick":
                dataQualifier = "T";
                break;
            case "minute": case "min": case "m":
                dataQualifier = "I";
                this.interval = "60";
                break;
            case "d": case "day":
                dataQualifier = "D";
                break;
            case "interval":
                dataQualifier = "I";
                break;
            case "w":
                dataQualifier = "W";
                break;
            case "wk":
                dataQualifier = "W";
                break;
            case "week":
                dataQualifier = "W";
                break;
            case "mo": case "month":
                dataQualifier = "M";
                break;
        }

        if(maxDataPts != null){
            timeQualifier = "X";
        } else if (maxDays != null){
            timeQualifier = "D";
        } else if (beginDateTime != null && endDateTime != null){
            timeQualifier = "T";
        } else {
            throw new InvalidParameterException("Invalid time Qualifier");
        }

        StringBuilder dataRequest = new StringBuilder("H");

        if((dataQualifier != null) && (timeQualifier != null) ) {
            dataRequest.append(dataQualifier);
            dataRequest.append(timeQualifier);
            dataRequest.append(",");
        }

        // arguments for historical requests follow the priority shown below
        List<String> argsOrder = Arrays.asList(symbol, interval, maxDays, maxWeeks, maxMonths, beginDateTime, beginDate,
                endDate, maxDataPts, beginFilterTime, endFilterTime, dataDirection, requestID, dataPtsPerSend, includePartialData,
                intervalType, labelAtBeginning);

        for(String arg: argsOrder){
            // TODO: convert to Objects.equals
            if(arg != "" && arg != null){
                dataRequest.append(arg + ",");
            }
        }
        // remove trailing comma
        dataRequest.deleteCharAt(dataRequest.length()-1);

        return dataRequest.toString();
    }

}
