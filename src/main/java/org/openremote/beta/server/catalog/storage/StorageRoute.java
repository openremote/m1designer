package org.openremote.beta.server.catalog.storage;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class StorageRoute extends NodeRoute {

    public StorageRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        // Nothing to do
    }
}
