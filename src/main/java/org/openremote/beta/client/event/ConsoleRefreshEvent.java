package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ConsoleRefreshEvent extends FlowEvent {

    final public String selectedNodeId;

    public ConsoleRefreshEvent() {
        this(null, null);
    }

    public ConsoleRefreshEvent(Flow flow) {
        this(flow, null);
    }

    public ConsoleRefreshEvent(Flow flow, String selectedNodeId) {
        super(flow);
        this.selectedNodeId = selectedNodeId;
    }

    public String getSelectedNodeId() {
        return selectedNodeId;
    }
}
