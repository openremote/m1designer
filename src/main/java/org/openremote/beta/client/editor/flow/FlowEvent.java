package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.flow.Flow;

@JsExport
@JsType
public abstract class FlowEvent extends Event {

    protected final Flow flow;

    public FlowEvent(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlow() {
        return flow;
    }
}
