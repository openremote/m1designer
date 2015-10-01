package org.openremote.client.shared.session;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class SessionClosedErrorEvent extends Event {

    @JsType
    public static class Error {

        public final int code;
        public final String reason;

        public Error(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }
    }

    protected final Error reason;

    public SessionClosedErrorEvent(Error reason) {
        this.reason = reason;
    }

    public Error getReason() {
        return reason;
    }
}
