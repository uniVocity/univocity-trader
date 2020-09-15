package com.univocity.trader.exchange.binance.futures.impl.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedList;

public class JsonWrapperArray {

    private JSONArray array = null;

    public JsonWrapperArray(JSONArray array) {
        this.array = array;
    }

    public JsonWrapper getJsonObjectAt(int index) {
        if (array != null && array.size() > index) {
            JSONObject object = (JSONObject) array.get(index);
            if (object == null) {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                        "[Json] Cannot get object at index " + index + " in array");
            }
            return new JsonWrapper(object);
        } else {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Index is out of bound or array is null");
        }
    }

    public void add(JSON val) {
        this.array.add(val);
    }

    public JsonWrapperArray getArrayAt(int index) {
        if (array != null && array.size() > index) {
            JSONArray newArray = (JSONArray) array.get(index);
            if (newArray == null) {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                        "[Json] Cannot get array at index " + index + " in array");
            }
            return new JsonWrapperArray(newArray);
        } else {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Index is out of bound or array is null");
        }
    }

    private Object getObjectAt(int index) {
        if (array != null && array.size() > index) {
            return array.get(index);
        } else {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Index is out of bound or array is null");
        }
    }

    public long getLongAt(int index) {
        try {
            return (Long) getObjectAt(index);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Cannot get long at index " + index + " in array: " + e.getMessage());
        }

    }

    public Integer getIntegerAt(int index) {
        try {
            return (Integer) getObjectAt(index);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Cannot get integer at index " + index + " in array: " + e.getMessage());
        }

    }

    public BigDecimal getBigDecimalAt(int index) {

        try {
            return new BigDecimal(new BigDecimal(getStringAt(index)).stripTrailingZeros().toPlainString());
        } catch (RuntimeException e) {
            throw new BinanceApiException(null, e.getMessage());
        }

    }

    public String getStringAt(int index) {

        try {
            return (String) getObjectAt(index);
        } catch (RuntimeException e) {
            throw new BinanceApiException(null, e.getMessage());
        }

    }

    public void forEach(Handler<JsonWrapper> objectHandler) {
        array.forEach((object) -> {
            if (!(object instanceof JSONObject)) {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR, "[Json] Parse array error in forEach");
            }
            objectHandler.handle(new JsonWrapper((JSONObject) object));
        });
    }

    public void forEachAsArray(Handler<JsonWrapperArray> objectHandler) {
        array.forEach((object) -> {
            if (!(object instanceof JSONArray)) {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                        "[Json] Parse array error in forEachAsArray");
            }
            objectHandler.handle(new JsonWrapperArray((JSONArray) object));
        });
    }

    public void forEachAsString(Handler<String> objectHandler) {
        array.forEach((object) -> {
            if (!(object instanceof String)) {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                        "[Json] Parse array error in forEachAsString");
            }
            objectHandler.handle((String) object);
        });
    }

    public List<String> convert2StringList() {
        List<String> result = new LinkedList<>();
        this.forEachAsString((item) -> {
            result.add(item);
        });
        return result;
    }

}
