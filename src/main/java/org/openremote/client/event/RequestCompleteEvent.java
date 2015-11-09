package org.openremote.client.event;

import jsinterop.annotations.JsType;
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
