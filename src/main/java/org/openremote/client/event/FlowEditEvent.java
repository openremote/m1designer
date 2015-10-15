package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;

@JsType
public class FlowEditEvent extends FlowEvent {

    final public boolean unsaved;

    public FlowEditEvent(Flow flow, boolean unsaved) {
        super(flow);
        this.unsaved = unsaved;
    }

    public boolean isUnsaved() {
        return unsaved;
    }
}
