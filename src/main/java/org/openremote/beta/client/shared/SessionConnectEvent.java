package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class SessionConnectEvent extends Event implements PropagationOptions {

    protected final String serviceUrl;

    public SessionConnectEvent(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    @Override
    public boolean isBubbling() {
        return false;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
