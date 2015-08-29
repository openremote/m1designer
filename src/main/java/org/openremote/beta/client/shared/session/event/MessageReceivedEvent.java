package org.openremote.beta.client.shared.session.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.Message;

@JsType
public class MessageReceivedEvent extends Event {

    final public Message message;

    public MessageReceivedEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
