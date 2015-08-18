package org.openremote.beta.client.shared;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.js.util.JsMapFromStringTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@JsExport
@JsType
public class Properties {

    private static final Logger LOG = LoggerFactory.getLogger(Properties.class);

    // TODO: Maybe this will not be necessary with better JSInterop
    public static JsMapFromStringTo<Object> getProperties(Map<String, Object> properties) {
        JsMapFromStringTo<Object> jsmap = JsMapFromStringTo.create();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                jsmap.put(entry.getKey(), entry.getValue());
            }
        }
        return jsmap;
    }

    public static void setProperties(JsMapFromStringTo<Object> jsmap, Map<String, Object> properties) {
        for (int i = 0; i < jsmap.keys().length(); i++) {
            String key = jsmap.keys().get(i);
            Object value = jsmap.values().get(i);
            if (!(value instanceof JsMapFromStringTo)) {
                properties.put(key, value);
            }
        }
    }

}
