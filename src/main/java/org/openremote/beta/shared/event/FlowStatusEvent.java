package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonTypeName("FLOW_STATUS")
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class FlowStatusEvent extends FlowIdEvent {

    public FlowDeploymentPhase phase;

    public FlowStatusEvent() {
        this(null, null);
    }

    public FlowStatusEvent(String flowId, FlowDeploymentPhase phase) {
        super(flowId);
        this.phase = phase;
    }

    public FlowDeploymentPhase getPhase() {
        return phase;
    }
}
