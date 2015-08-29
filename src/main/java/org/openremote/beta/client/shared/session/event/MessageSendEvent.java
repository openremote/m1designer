package org.openremote.beta.client.shared.session.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.Message;

@JsType
public class MessageSendEvent extends Event {

    final public Message message;

    public MessageSendEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
