package org.openremote.beta.client.shell.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class FlowSavedEvent extends FlowEvent {

    public FlowSavedEvent(Flow flow) {
        super(flow);
    }
}
