package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class FlowNodeOpenEvent extends Event {

    final protected String editorComponent;

    public FlowNodeOpenEvent(String editorComponent) {
        this.editorComponent = editorComponent;
    }

    public String getEditorComponent() {
        return editorComponent;
    }
}
