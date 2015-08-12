package org.openremote.beta.client.shared.session;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.PropagationOptions;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class SessionClosedCleanEvent extends Event implements PropagationOptions {

    @Override
    public boolean isBubbling() {
        return false;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
