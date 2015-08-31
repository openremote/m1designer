package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class FlowDeletedEvent extends FlowEvent {

    public FlowDeletedEvent(Flow flow) {
        super(flow);
    }
}