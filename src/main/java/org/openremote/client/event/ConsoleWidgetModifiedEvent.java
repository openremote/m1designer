package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class ConsoleWidgetModifiedEvent extends Event {

    final public String nodeId;
    final public String nodeProperties;

    public ConsoleWidgetModifiedEvent(String nodeId, String nodeProperties) {
        this.nodeId = nodeId;
        this.nodeProperties = nodeProperties;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeProperties() {
        return nodeProperties;
    }
}