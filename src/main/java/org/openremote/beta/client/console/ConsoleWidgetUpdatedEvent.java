package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConsoleWidgetUpdatedEvent extends Event {

    final public String nodeId;
    final public String properties;

    public ConsoleWidgetUpdatedEvent(String nodeId, String properties) {
        this.nodeId = nodeId;
        this.properties = properties;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getProperties() {
        return properties;
    }
}
