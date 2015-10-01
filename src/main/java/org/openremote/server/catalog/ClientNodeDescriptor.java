package org.openremote.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.server.route.ClientRoute;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.NodeColor;

public abstract class ClientNodeDescriptor extends NodeDescriptor {

    @Override
    public NodeColor getColor() {
        return NodeColor.CLIENT;
    }

    @Override
    public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
        return new ClientRoute(context, flow, node);
    }

    @Override
    public Node initialize(Node node) {
        node = super.initialize(node);
        node.setClientAccess(true);
        return node;
    }
}
