package org.openremote.beta.client.flow.crud;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.FlowIdEvent;

@JsExport
@JsType
public class FlowLoadEvent extends FlowIdEvent {

    public FlowLoadEvent(String flowId) {
        super(flowId);
    }
}
