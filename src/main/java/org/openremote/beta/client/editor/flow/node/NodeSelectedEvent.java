package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.flow.Node;

@JsType
public class NodeSelectedEvent extends Event {

    final public Node node;

    public NodeSelectedEvent(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
