package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ShowInfoEvent extends Event {

    final public String text;

    public ShowInfoEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
