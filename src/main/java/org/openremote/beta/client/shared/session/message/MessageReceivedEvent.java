package org.openremote.beta.client.shared.session.message;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.MessageEvent;

@JsExport
@JsType
public class MessageReceivedEvent extends Event implements NonBubblingEvent {

    final protected MessageEvent messageEvent;

    public MessageReceivedEvent(MessageEvent messageEvent) {
        this.messageEvent = messageEvent;

        // TODO
        /*
        Map<String, Object> headers = createMap();
        headers.put("FOO", "fff");
        headers.put("BAR", "bbb");
        this.messageEvent.setHeaders(headers);
        */
    }

    public MessageEvent getMessageEvent() {
        return messageEvent;
    }
}
