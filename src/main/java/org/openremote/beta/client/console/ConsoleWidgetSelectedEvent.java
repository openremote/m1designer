package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConsoleWidgetSelectedEvent extends Event {

    final public String nodeId;

    public ConsoleWidgetSelectedEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
