package org.openremote.server.catalog.gate;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.LoggingLevel.DEBUG;

public class NotRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(NotRoute.class);

    public NotRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId("NOT"))
            .when(isInputTrue())
            .log(DEBUG, LOG, "Input is true, negating")
            .setBody(constant(0))
            .when(isInputFalse())
            .log(DEBUG, LOG, "Input is false, negating")
            .setBody(constant(1))
            .otherwise()
            .log(DEBUG, LOG, "Input is not boolean, stopping")
            .stop()
            .endChoice();
    }
}
