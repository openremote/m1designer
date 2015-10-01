package org.openremote.client.shared;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class ShowFailureEvent extends Event {

    public static final int DURABLE = 9999999; // 7 days...

    final public String text;
    final public int durationMillis;

    public ShowFailureEvent(String text) {
        this(text, DURABLE);
    }

    public ShowFailureEvent(String text, int durationMillis) {
        this.text = text;
        this.durationMillis = durationMillis;
    }

    public String getText() {
        return text;
    }

    public int getDurationMillis() {
        return durationMillis;
    }
}
