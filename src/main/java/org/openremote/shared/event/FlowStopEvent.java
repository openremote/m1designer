package org.openremote.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gwt.core.client.js.JsType;

@JsType
@JsonTypeName("FLOW_STOP")
public class FlowStopEvent extends FlowIdEvent {

    protected FlowStopEvent() {
    }

    public FlowStopEvent(String flowId) {
        super(flowId);
    }

}
