package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowSavedEvent extends FlowEvent {

    public FlowSavedEvent(Flow flow) {
        super(flow);
    }
}
