package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.flow.Flow;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class FlowStopEvent extends FlowEvent {

    public FlowStopEvent() {
    }

    public FlowStopEvent(String flowId) {
        super(flowId);
    }

    public FlowStopEvent(Flow flow) {
        super(flow);
    }

}
