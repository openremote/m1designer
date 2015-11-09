package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowDeletedEvent extends FlowEvent {

    public FlowDeletedEvent(Flow flow) {
        super(flow);
    }
}
