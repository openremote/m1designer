package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class ShowFailureMessageEvent extends Event {

    protected final String message;

    public ShowFailureMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
