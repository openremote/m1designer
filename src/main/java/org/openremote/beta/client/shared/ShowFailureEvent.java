package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class ShowFailureEvent extends Event {

    protected final String message;

    public ShowFailureEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
