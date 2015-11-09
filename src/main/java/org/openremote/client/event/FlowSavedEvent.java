package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowSavedEvent extends FlowEvent {

    public FlowSavedEvent(Flow flow) {
        super(flow);
    }
}
