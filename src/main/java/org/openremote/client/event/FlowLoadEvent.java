package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.FlowIdEvent;

@JsType
public class FlowLoadEvent extends FlowIdEvent {

    public FlowLoadEvent(String flowId) {
        super(flowId);
    }
}
