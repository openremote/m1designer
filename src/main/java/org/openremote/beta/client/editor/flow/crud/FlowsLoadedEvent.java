package org.openremote.beta.client.editor.flow.crud;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.flow.Flow;

@JsExport
@JsType
public class FlowsLoadedEvent extends Event {

    protected final Flow[] flows;

    public FlowsLoadedEvent(Flow[] flows) {
        this.flows = flows;
    }

    public Flow[] getFlows() {
        return flows;
    }

}
