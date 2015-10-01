package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

@JsType
public class NodeModifiedEvent extends FlowEvent {

    final public Node node;

    public NodeModifiedEvent(Flow flow, Node node) {
        super(flow);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
