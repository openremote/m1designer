package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.Event;

@JsType
public class ServerSendEvent extends Event {

    final public Event event;

    public ServerSendEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
