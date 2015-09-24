package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

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
