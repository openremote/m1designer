package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
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
