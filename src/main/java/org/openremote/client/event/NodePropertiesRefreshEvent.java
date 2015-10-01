package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
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