package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class RequestCompleteEvent extends Event {

    final public String message;

    public RequestCompleteEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
