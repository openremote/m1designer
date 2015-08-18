package org.openremote.beta.client.editor.flow.crud;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsExport
@JsType
public class FlowSavedEvent extends FlowEvent {

    public FlowSavedEvent(Flow flow) {
        super(flow);
    }
}
