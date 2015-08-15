package org.openremote.beta.shared.util;

import java.util.HashMap;
import java.util.Locale;
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
        return map.containsKey(key) ? map.get(key).toString() : null;
    }

    @SuppressWarnings("unchecked")
    public static Double getDouble(Map<String, Object> map, String key) {
        return map.containsKey(key) ? Double.valueOf(map.get(key).toString()) : null;
    }

    @SuppressWarnings("unchecked")
    public static boolean getBoolean(Map<String, Object> map, String key) {
        return map.containsKey(key) ? Boolean.valueOf(map.get(key).toString()) : false;
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
        // Transforms 'EXFooBar123' into 'ex-foo-bar-123 and "attributeX" into "attribute-x" without regex (GWT!)
        if (camelCase == null)
            return null;
        if (camelCase.length() == 0)
            return camelCase;
        StringBuilder sb = new StringBuilder();
        char[] chars = camelCase.toCharArray();
        boolean inNonLowerCase = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (!Character.isLowerCase(c)) {
                if (!inNonLowerCase) {
                    if (i > 0)
                        sb.append("-");
                } else if (i < chars.length -1 && Character.isLowerCase(chars[i+1])) {
                    sb.append("-");
                }
                inNonLowerCase = true;
            } else {
                inNonLowerCase = false;
            }
            sb.append(c);
        }
        String name = sb.toString();
        name = name.toLowerCase(Locale.ROOT);
        return name;
    }

}
