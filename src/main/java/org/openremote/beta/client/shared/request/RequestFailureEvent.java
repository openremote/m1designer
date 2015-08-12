package org.openremote.beta.client.shared.request;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class RequestFailureEvent extends Event {

    protected final RequestFailure requestFailure;

    public RequestFailureEvent(RequestFailure requestFailure) {
        this.requestFailure = requestFailure;
    }

    public RequestFailure getRequestFailure() {
        return requestFailure;
    }

}
