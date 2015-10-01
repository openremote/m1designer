package org.openremote.client.shared.session.event;

import com.google.gwt.core.client.js.JsType;
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
