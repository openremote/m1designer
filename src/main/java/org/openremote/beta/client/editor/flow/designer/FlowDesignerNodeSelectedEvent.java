package org.openremote.beta.client.editor.flow.designer;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.flow.Node;

@JsType
public class FlowDesignerNodeSelectedEvent extends Event {

    final protected Node node;

    public FlowDesignerNodeSelectedEvent(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
