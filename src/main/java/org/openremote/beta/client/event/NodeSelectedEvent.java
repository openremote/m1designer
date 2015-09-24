package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

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
