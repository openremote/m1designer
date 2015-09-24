package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

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
