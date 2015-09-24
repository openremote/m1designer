package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

@JsType
public class NodeDeleteEvent extends FlowEvent {

    final public Node node;

    public NodeDeleteEvent(Flow flow, Node node) {
        super(flow);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
