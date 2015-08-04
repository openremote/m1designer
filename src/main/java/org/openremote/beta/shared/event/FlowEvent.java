package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.flow.Flow;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSubTypes({
    @Type(value = FlowStartEvent.class),
    @Type(value = FlowStartedEvent.class),
    @Type(value = FlowStopEvent.class),
    @Type(value = FlowStoppedEvent.class),
})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class FlowEvent {

    public String flowId;

    public FlowEvent() {
    }

    public FlowEvent(String flowId) {
        this.flowId = flowId;
    }

    public FlowEvent(Flow flow) {
        this(flow.getIdentifier().getId());
    }

    public String getFlowId() {
        return flowId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "flowId='" + flowId + '\'' +
            '}';
    }
}
