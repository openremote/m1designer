package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ShowFailureEvent extends Event {

    public static final int DURABLE = 9999999; // 7 days...

    final protected String message;
    final protected int durationMillis;

    public ShowFailureEvent(String message, int durationMillis) {
        this.message = message;
        this.durationMillis = durationMillis;
    }

    public String getMessage() {
        return message;
    }

    public int getDurationMillis() {
        return durationMillis;
    }
}
