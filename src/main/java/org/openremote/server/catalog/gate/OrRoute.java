package org.openremote.server.catalog.gate;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrRoute extends GateRoute {

    private static final Logger LOG = LoggerFactory.getLogger(OrRoute.class);

    public OrRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        super.configureProcessing(routeDefinition);

        routeDefinition
            .process(exchange -> {
                Gate gate = getInstanceState(exchange);
                if (gate.a || gate.b) {
                    LOG.debug("a OR b is true");
                    exchange.getIn().setBody(1);
                } else {
                    LOG.debug("a OR b is false");
                    exchange.getIn().setBody(0);
                }
            });
    }

}
