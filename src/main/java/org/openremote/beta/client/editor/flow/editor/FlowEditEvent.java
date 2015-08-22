package org.openremote.beta.client.editor.flow.editor;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class FlowEditEvent extends FlowEvent {

    final protected boolean unsaved;

    public FlowEditEvent(Flow flow, boolean unsaved) {
        super(flow);
        this.unsaved = unsaved;
    }

    public boolean isUnsaved() {
        return unsaved;
    }
}
