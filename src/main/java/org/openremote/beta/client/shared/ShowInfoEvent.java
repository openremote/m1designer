package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class ShowInfoEvent extends Event {

    protected final String message;

    public ShowInfoEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
