package org.openremote.beta.client.shared.session;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.PropagationOptions;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class SessionClosedErrorEvent extends Event implements PropagationOptions {

    @JsExport
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

    @Override
    public boolean isBubbling() {
        return false;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
