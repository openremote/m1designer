package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shell.event.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ConsoleRefreshEvent extends FlowEvent {

    final public boolean dirty;
    final public String selectedNodeId;

    public ConsoleRefreshEvent(Flow flow, boolean dirty, String selectedNodeId) {
        super(flow);
        this.dirty = dirty;
        this.selectedNodeId = selectedNodeId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public String getSelectedNodeId() {
        return selectedNodeId;
    }
}
