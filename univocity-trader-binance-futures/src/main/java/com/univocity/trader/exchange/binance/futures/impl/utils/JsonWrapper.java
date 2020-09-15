package com.univocity.trader.exchange.binance.futures.impl.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class JsonWrapper {

    private final JSONObject json;

    public static JsonWrapper parseFromString(String text) {
        try {
            JSONObject jsonObject;
            if(JSON.parse(text) instanceof JSONArray) {
                jsonObject = (JSONObject) JSON.parse("{data:" + text + "}");
            } else {
                jsonObject = (JSONObject) JSON.parse(text);
            }
            if (jsonObject != null) {
                return new JsonWrapper(jsonObject);
            } else {
                throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                        "[Json] Unknown error when parse: " + text);
            }
        } catch (JSONException e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR, "[Json] Fail to parse json: " + text);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR, "[Json] " + e.getMessage());
        }
    }

    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    private void checkMandatoryField(String name) {
        if (!json.containsKey(name)) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get json item field: " + name + " does not exist");
        }
    }

    public boolean containKey(String name) {
        return json.containsKey(name);
    }

    public String getString(String name) {
        checkMandatoryField(name);
        try {
            return json.getString(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get string error: " + name + " " + e.getMessage());
        }
    }

    public String getStringOrDefault(String name, String def) {
        if (!containKey(name)) {
            return def;
        }
        return getString(name);
    }

    public Boolean getBooleanOrDefault(String name, Boolean defaultValue) {
        if (!containKey(name)) {
            return defaultValue;
        }
        return getBoolean(name);
    }

    public boolean getBoolean(String name) {
        checkMandatoryField(name);
        try {
            return json.getBoolean(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get boolean error: " + name + " " + e.getMessage());
        }
    }

    public int getInteger(String name) {
        checkMandatoryField(name);
        try {
            return json.getInteger(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get integer error: " + name + " " + e.getMessage());
        }
    }

    public Integer getIntegerOrDefault(String name, Integer defValue) {
        try {
            if (!containKey(name)) {
                return defValue;
            }
            return json.getInteger(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get integer error: " + name + " " + e.getMessage());
        }
    }

    public long getLong(String name) {
        checkMandatoryField(name);
        try {
            return json.getLong(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get long error: " + name + " " + e.getMessage());
        }
    }

    public long getLongOrDefault(String name, long defValue) {
        try {
            if (!containKey(name)) {
                return defValue;
            }
            return json.getLong(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get long error: " + name + " " + e.getMessage());
        }
    }
    public Double getDouble(String name) {
        checkMandatoryField(name);
        try {
            return json.getDouble(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get double error: " + name + " " + e.getMessage());
        }
    }

    public Double getDoubleOrDefault(String name, Double defValue) {
        try {
            if (!containKey(name)) {
                return defValue;
            }
            return json.getDouble(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get double error: " + name + " " + e.getMessage());
        }
    }

    public BigDecimal getBigDecimal(String name) {
        checkMandatoryField(name);
        try {
            return new BigDecimal(json.getBigDecimal(name).stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get decimal error: " + name + " " + e.getMessage());
        }
    }

    public BigDecimal getBigDecimalOrDefault(String name, BigDecimal defValue) {
        if (!containKey(name)) {
            return defValue;
        }
        try {
            return new BigDecimal(json.getBigDecimal(name).stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Get decimal error: " + name + " " + e.getMessage());
        }
    }

    public JsonWrapper getJsonObject(String name) {
        checkMandatoryField(name);
        return new JsonWrapper(json.getJSONObject(name));
    }

    public JSONObject convert2JsonObject() {
        return this.json;
    }

    public void getJsonObject(String name, Handler<JsonWrapper> todo) {
        checkMandatoryField(name);
        todo.handle(new JsonWrapper(json.getJSONObject(name)));
    }

    public JsonWrapperArray getJsonArray(String name) {
        checkMandatoryField(name);
        JSONArray array = null;
        try {
            array = json.getJSONArray(name);
        } catch (Exception e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR, "[Json] Get array: " + name + " error");
        }
        if (array == null) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Json] Array: " + name + " does not exist");
        }
        return new JsonWrapperArray(array);
    }

    public JSONObject getJson() {
        return json;
    }

    public List<Map<String, String>> convert2DictList() {
        List<Map<String, String>> result = new LinkedList<>();
        Set<String> keys = this.json.keySet();
        keys.forEach((key) -> {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put(key, this.getString(key));
            result.add(temp);
        });
        return result;
    }

}
