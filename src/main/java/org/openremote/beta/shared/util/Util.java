package org.openremote.beta.shared.util;

import java.util.HashMap;
import java.util.Map;

public class Util {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Object map) {
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static String getString(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Double getDouble(Map<String, Object> map, String key) {
        return (Double) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createMap() {
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createMap(Map<String, Object> map, String key) {
        Map<String, Object> newMap = new HashMap<>();
        map.put(key, newMap);
        return newMap;
    }
}
