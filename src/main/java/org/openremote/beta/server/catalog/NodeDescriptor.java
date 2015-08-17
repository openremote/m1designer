package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;

public abstract class NodeDescriptor {

    abstract public String getType();

    abstract public String getTypeLabel();

    abstract public NodeRoute createRoute(CamelContext context, Flow flow, Node node);

    public boolean isInternal() {
        return false;
    }

    public NodeColor getColor() {
        return NodeColor.DEFAULT;
    }
}
