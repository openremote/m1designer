package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class FlowItem {

    private static final Logger LOG = LoggerFactory.getLogger(FlowItem.class);

    final public Flow flow;
    public FlowStatusDetail status;
    public String statusClass;

    public FlowItem(Flow flow, FlowStatusDetail status) {
        this.flow = flow;
        setStatus(status);
    }

    public Flow getFlow() {
        return flow;
    }

    public FlowStatusDetail getStatus() {
        return status;
    }

    public void setStatus(FlowStatusDetail status) {
        this.status = status;
        setStatusClass();
    }

    public void setStatusClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("status");
        if (getStatus() != null && getStatus().mark != null) {
            sb.append(" ").append(getStatus().mark);
        }
        statusClass = sb.toString();
    }
}
