package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowDeletedEvent extends FlowEvent {

    public FlowDeletedEvent(Flow flow) {
        super(flow);
    }
}
