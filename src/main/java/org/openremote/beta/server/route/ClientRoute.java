package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.catalog.ClientNodeDescriptor;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRoute.class);

    public ClientRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        // Do nothing
    }

}
