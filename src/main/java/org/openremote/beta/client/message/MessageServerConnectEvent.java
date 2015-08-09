package org.openremote.beta.client.message;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.PropagationOptions;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class MessageServerConnectEvent extends Event implements PropagationOptions {

    @Override
    public boolean isBubbling() {
        return false;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
