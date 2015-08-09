package org.openremote.beta.shared.event;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public abstract class Event {

    private static final Logger LOG = LoggerFactory.getLogger(Event.class);

    @JsNoExport
    public static String getType(String simpleClassName) {
        String type = Util.toLowerCaseDash(simpleClassName);

        if (type.length() > 6 && type.substring(type.length()-6).equals("-event"))
            type = type.substring(0, type.length()-6);

        return type;
    }

    @JsNoExport
    public static String getType(Class<? extends Event> actionClass) {
        return getType(actionClass.getSimpleName());
    }

    public String getType() {
        return getType(getClass());
    }

}
