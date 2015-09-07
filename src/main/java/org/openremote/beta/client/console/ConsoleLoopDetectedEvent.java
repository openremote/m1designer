package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConsoleLoopDetectedEvent extends Event {

    final public String nodeId;
    final public String nodeLabel;

    public ConsoleLoopDetectedEvent(String nodeId, String nodeLabel) {
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }
}
