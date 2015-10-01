package org.openremote.shared.model;

import java.util.HashMap;
import java.util.Map;

public class Properties {

    public static Map<String, Object> create() {
        return new HashMap<>();
    }

    public static Map<String, Object> create(Map<String, Object> properties, String key) {
        Map<String, Object> newProperties = new HashMap<>();
        properties.put(key, newProperties);
        return newProperties;
    }

    public static String get(Map<String, Object> properties, String key) {
        return get(properties, PropertyDescriptor.TYPE_STRING, key);
    }

    public static <T> T get(Map<String, Object> properties, PropertyDescriptor<T> descriptor, String key) {
        return properties != null && properties.containsKey(key) ? descriptor.read(properties.get(key)) : null;
    }

    public static boolean isTrue(Map<String, Object> properties, String key) {
        return isTrue(properties, PropertyDescriptor.TYPE_BOOLEAN, key);
    }

    public static boolean isTrue(Map<String, Object> properties, PropertyDescriptor<Boolean> descriptor, String key) {
        Boolean result = get(properties, descriptor, key);
        return result != null && result;
    }

    public static boolean isSet(Map<String, Object> properties, String key) {
        return properties != null && properties.containsKey(key);
    }

    public static Map<String, Object> getProperties(Map<String, Object> properties, String key) {
        if (!containsProperties(properties, key))
            return null;
        //noinspection unchecked
        return (Map<String, Object>) properties.get(key);
    }

    public static boolean containsProperties(Map<String, Object> properties, String key) {
        return properties.containsKey(key) && properties.get(key) instanceof Map;
    }

}
