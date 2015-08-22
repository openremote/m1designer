package org.openremote.beta.client.editor.flow.editor;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class FlowUpdatedEvent extends FlowEvent {

    public FlowUpdatedEvent(Flow flow) {
        super(flow);
    }

}
