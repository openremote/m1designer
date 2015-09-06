package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ConsoleRefreshEvent extends FlowEvent {

    final public boolean dirty;

    public ConsoleRefreshEvent(Flow flow, boolean dirty) {
        super(flow);
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }
}
