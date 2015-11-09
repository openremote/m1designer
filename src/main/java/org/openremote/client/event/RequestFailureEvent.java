package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.Event;

@JsType
public class RequestFailureEvent extends Event {

    final public RequestFailure requestFailure;

    public RequestFailureEvent(RequestFailure requestFailure) {
        this.requestFailure = requestFailure;
    }

    public RequestFailure getRequestFailure() {
        return requestFailure;
    }

}
