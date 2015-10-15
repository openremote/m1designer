package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowModifiedEvent extends FlowEvent {

    final public boolean notifyConsole;

    public FlowModifiedEvent(Flow flow, boolean notifyConsole) {
        super(flow);
        this.notifyConsole = notifyConsole;
    }

    public boolean isNotifyConsole() {
        return notifyConsole;
    }
}
