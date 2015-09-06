package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ConsoleSwitchEvent extends FlowEvent {

    public boolean maximized;

    public ConsoleSwitchEvent(Flow flow, boolean maximized) {
        super(flow);
        this.maximized = maximized;
    }

    public boolean isMaximized() {
        return maximized;
    }
}
