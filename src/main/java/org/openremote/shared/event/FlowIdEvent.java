package org.openremote.shared.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.flow.Flow;

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

    public boolean matches(Flow other) {
        return other != null && other.getId().equals(getFlowId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "flowId='" + flowId + '\'' +
            '}';
    }
}
