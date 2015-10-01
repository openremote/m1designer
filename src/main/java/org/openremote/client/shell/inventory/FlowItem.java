package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.js.JsType;
import org.openremote.client.shell.flowcontrol.FlowStatusDetail;
import org.openremote.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class FlowItem {

    private static final Logger LOG = LoggerFactory.getLogger(FlowItem.class);

    final public Flow flow;
    public FlowStatusDetail status;

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
    }
}
