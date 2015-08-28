package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.shared.event.MessageEvent;

@JsType
public class ConsoleMessageSendEvent extends MessageSendEvent {

    public ConsoleMessageSendEvent(MessageEvent messageEvent) {
        super(messageEvent);
    }
}
