package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConsoleWidgetSelectEvent extends Event {

    final public String nodeId;

    public ConsoleWidgetSelectEvent(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
