package org.openremote.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gwt.core.client.js.JsType;

@JsType
@JsonTypeName("FLOW_DEPLOY")
public class FlowDeployEvent extends FlowIdEvent {

    protected FlowDeployEvent() {
    }

    public FlowDeployEvent(String flowId) {
        super(flowId);
    }

}
