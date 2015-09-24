package org.openremote.beta.client.shell.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class NodeSelectEvent extends Event {

    final public String nodeId;

    public NodeSelectEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
