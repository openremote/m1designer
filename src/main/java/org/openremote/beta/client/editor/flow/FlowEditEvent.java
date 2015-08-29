package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

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
