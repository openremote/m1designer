package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.flow.Flow;

@JsType
public abstract class FlowEvent extends Event {

    final public Flow flow;

    public FlowEvent(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlow() {
        return flow;
    }
}
