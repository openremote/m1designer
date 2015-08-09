package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class SessionOpenedEvent extends Event implements PropagationOptions {

    @Override
    public boolean isBubbling() {
        return false;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
