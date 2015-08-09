package org.openremote.beta.client.flow.node;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsExport
@JsType
public class FlowNodeCloseEvent extends FlowEvent {

    public FlowNodeCloseEvent(Flow flow) {
        super(flow);
    }
}
