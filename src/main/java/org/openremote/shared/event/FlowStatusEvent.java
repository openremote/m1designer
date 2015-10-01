package org.openremote.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gwt.core.client.js.JsType;

@JsType
@JsonTypeName("FLOW_STATUS")
public class FlowStatusEvent extends FlowIdEvent {

    public FlowDeploymentPhase phase;

    protected FlowStatusEvent() {
    }

    public FlowStatusEvent(String flowId, FlowDeploymentPhase phase) {
        super(flowId);
        this.phase = phase;
    }

    public FlowDeploymentPhase getPhase() {
        return phase;
    }
}
