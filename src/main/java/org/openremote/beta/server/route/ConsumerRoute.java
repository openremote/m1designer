package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerRoute.class);

    public ConsumerRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        // Nothing to do
    }

    @Override
    protected boolean isPublishingMessageEvents() {
        return true;
    }
}
