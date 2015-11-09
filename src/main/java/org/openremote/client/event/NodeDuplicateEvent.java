package org.openremote.client.event;

import jsinterop.annotations.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

@JsType
public class NodeDuplicateEvent extends FlowEvent {

    final public Node node;

    public NodeDuplicateEvent(Flow flow, Node node) {
        super(flow);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
