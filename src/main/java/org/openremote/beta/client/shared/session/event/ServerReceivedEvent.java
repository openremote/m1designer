package org.openremote.beta.client.shared.session.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;

@JsType
public class ServerReceivedEvent extends Event implements NonBubblingEvent {

    final public Event event;

    public ServerReceivedEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
