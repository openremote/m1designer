package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.FlowIdEvent;

@JsType
public class FlowLoadEvent extends FlowIdEvent {

    public FlowLoadEvent(String flowId) {
        super(flowId);
    }
}
