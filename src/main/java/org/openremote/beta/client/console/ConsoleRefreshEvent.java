package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ConsoleRefreshEvent extends FlowEvent {

    public ConsoleRefreshEvent(Flow flow) {
        super(flow);
    }
}
