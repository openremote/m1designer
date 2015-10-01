package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;
import org.openremote.shared.flow.Flow;

@JsType
public abstract class FlowEvent extends Event {

    final public Flow flow;

    public FlowEvent(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlow() {
        return flow;
    }

    public boolean matches(Flow other) {
        return other != null && other.getId().equals(flow.getId());
    }
}
