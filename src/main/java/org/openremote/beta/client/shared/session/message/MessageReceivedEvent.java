package org.openremote.beta.client.shared.session.message;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.MessageEvent;

@JsType
public class MessageReceivedEvent extends Event implements NonBubblingEvent {

    final protected MessageEvent messageEvent;

    public MessageReceivedEvent(MessageEvent messageEvent) {
        this.messageEvent = messageEvent;
    }

    public MessageEvent getMessageEvent() {
        return messageEvent;
    }
}
