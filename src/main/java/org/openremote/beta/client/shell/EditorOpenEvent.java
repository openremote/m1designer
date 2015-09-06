package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class EditorOpenEvent extends Event {

    public final String flowId;

    public EditorOpenEvent() {
        this(null);
    }

    public EditorOpenEvent(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowId() {
        return flowId;
    }
}
