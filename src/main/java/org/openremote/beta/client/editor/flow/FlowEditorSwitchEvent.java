package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;

@JsType
public class FlowEditorSwitchEvent extends Event implements NonBubblingEvent{

    final public boolean visible;

    public FlowEditorSwitchEvent(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
