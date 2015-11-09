package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.Event;

@JsType
public class NodePropertiesRefreshEvent extends Event {

    final public String nodeId;

    public NodePropertiesRefreshEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}