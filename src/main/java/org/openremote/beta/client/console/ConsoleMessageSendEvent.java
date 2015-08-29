package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.shared.event.Message;

@JsType
public class ConsoleMessageSendEvent extends MessageSendEvent {

    public ConsoleMessageSendEvent(Message message) {
        super(message);
    }
}
