package org.openremote.beta.client.shell;


import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class MessageLogUpdateEvent extends Event {

    protected final MessageLogDetail detail;

    public MessageLogUpdateEvent(MessageLogDetail detail) {
        this.detail = detail;
    }

    public MessageLogDetail getDetail() {
        return detail;
    }
}
