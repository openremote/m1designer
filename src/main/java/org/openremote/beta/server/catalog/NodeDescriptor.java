package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;

public abstract class NodeDescriptor {

    abstract public String getType();

    abstract public String getTypeLabel();

    abstract public NodeRoute createRoute(CamelContext context, Flow flow, Node node);

    public boolean isInternal() {
        return false;
    }

    public boolean isClientAccessEnabled() {
        return false;
    }

    public NodeColor getColor() {
        return NodeColor.DEFAULT;
    }

    public Slot[] createSlots() {
        return new Slot[0];
    }

    public Node initialize(Node node) {
        if (isClientAccessEnabled()) {
            node.getProperties().put(Node.PROPERTY_CLIENT_ACCESS, true);
        }

        node.getEditorProperties().put(Node.EDITOR_PROPERTY_COLOR, getColor().name());
        node.getEditorProperties().put(Node.EDITOR_PROPERTY_TYPE_LABEL, getTypeLabel());
        return node;
    }
}
