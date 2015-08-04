package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.flow.Flow;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class FlowStartEvent extends FlowEvent {

    public FlowStartEvent() {
    }

    public FlowStartEvent(String flowId) {
        super(flowId);
    }

    public FlowStartEvent(Flow flow) {
        super(flow);
    }

}
