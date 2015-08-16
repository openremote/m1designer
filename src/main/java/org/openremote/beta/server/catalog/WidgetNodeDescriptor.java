package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.route.ClientRoute;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public abstract class WidgetNodeDescriptor extends ClientNodeDescriptor {

    @Override
    public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
        return new ClientRoute(context, flow, node);
    }
}
