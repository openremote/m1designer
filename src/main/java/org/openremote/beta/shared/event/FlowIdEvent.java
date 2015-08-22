package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSubTypes({
    @Type(value = FlowStatusEvent.class),
    @Type(value = FlowRequestStatusEvent.class),
    @Type(value = FlowDeployEvent.class),
    @Type(value = FlowStopEvent.class),
    @Type(value = FlowDeploymentFailureEvent.class),
})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "event"
)
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class FlowIdEvent extends Event {

    public String flowId;

    public FlowIdEvent() {
    }

    public FlowIdEvent(String flowId) {
        this.flowId = flowId;
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
