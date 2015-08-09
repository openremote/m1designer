package org.openremote.beta.shared.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Util {

    protected final static char[] LOWER_CASE_ALPHA = "abcdefghijklmnopqrstuvwxyz".toCharArray();

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

    public static String toLowerCaseDash(String camelCase) {
        // Don't have regex that works on both client and server, so this
        // transforms 'EXFooBar123' into 'ex-foo-bar-123
        if (camelCase == null)
            return null;
        if (camelCase.length() == 0)
            return camelCase;
        StringBuilder sb = new StringBuilder();
        char[] chars = camelCase.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (!isLowerCaseAlpha(c)
                && i > 0
                && i<chars.length-1
                && sb.length() > 1
                && sb.charAt(sb.length()-2) != '-') {
                sb.append("-");
            };
            sb.append(c);
        }
        String name = sb.toString();
        name = name.toLowerCase(Locale.ROOT);
        return name;
    }

    protected static boolean isLowerCaseAlpha(char c) {
        for (char u : LOWER_CASE_ALPHA)
            if (c == u)
                return true;
        return false;
    }
}
