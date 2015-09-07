package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gwt.core.client.js.JsType;

@JsType
@JsonTypeName("FLOW_RUNTIME_FAILURE")
public class FlowRuntimeFailureEvent extends FlowIdEvent {

    public String message;
    public String nodeId;

    protected FlowRuntimeFailureEvent() {
        this(null, null);
    }

    public FlowRuntimeFailureEvent(String flowId, String message) {
        this(flowId, message, null);
    }

    public FlowRuntimeFailureEvent(String flowId, String message, String nodeId) {
        super(flowId);
        this.message = message;
        this.nodeId = nodeId;
    }

    public String getMessage() {
        return message;
    }

    public String getNodeId() {
        return nodeId;
    }

}
