package org.openremote.beta.client.editor.flow.control;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class FlowControlStartEvent extends FlowEvent implements NonBubblingEvent{

    final public boolean unsaved;

    public FlowControlStartEvent(Flow flow, boolean unsaved) {
        super(flow);
        this.unsaved = unsaved;
    }

    public boolean isUnsaved() {
        return unsaved;
    }
}
