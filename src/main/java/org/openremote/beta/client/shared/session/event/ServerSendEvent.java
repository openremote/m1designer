package org.openremote.beta.client.shared.session.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

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
