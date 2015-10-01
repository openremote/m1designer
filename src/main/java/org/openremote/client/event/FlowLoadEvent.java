package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.FlowIdEvent;

@JsType
public class FlowLoadEvent extends FlowIdEvent {

    public FlowLoadEvent(String flowId) {
        super(flowId);
    }
}
