package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class NodeSelectedEvent extends Event {

    final public String nodeId;

    public NodeSelectedEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
