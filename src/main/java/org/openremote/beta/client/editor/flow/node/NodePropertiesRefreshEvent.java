package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class NodePropertiesRefreshEvent extends Event {

    protected final String nodeId;

    public NodePropertiesRefreshEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}