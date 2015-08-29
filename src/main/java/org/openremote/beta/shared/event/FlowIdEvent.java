package org.openremote.beta.shared.event;

import com.google.gwt.core.client.js.JsType;

@JsType
public class FlowIdEvent extends Event {

    public String flowId;

    protected FlowIdEvent() {
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
