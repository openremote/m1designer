package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.server.route.FlowRoute.DESTINATION_SINK_ID;
import static org.openremote.beta.server.route.RouteManagementUtil.*;

public class SinkRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SinkRoute.class);

    protected final Flow flow;
    protected final Node node;
    protected final Slot sink;

    public SinkRoute(CamelContext context, Flow flow, Node node, Slot sink) {
        super(context);
        this.flow = flow;
        this.node = node;
        this.sink = sink;
        LOG.debug("Creating builder for: " + sink);
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + sink);

        from("direct:" + sink.getIdentifier().getId())
            .routeId(getRouteId(flow, node, sink))
            .routeDescription(getRouteDescription(flow, node, sink))
            .autoStartup(false)

            .log(LoggingLevel.DEBUG, LOG, "Sink processing: " + getRouteDescription(flow, node, sink))

            .setHeader(DESTINATION_SINK_ID, constant(sink.getIdentifier().getId()))
            .id(getProcessorId(flow, node, sink, "setDestinationSink"))

            .to("direct:" + node.getIdentifier().getId())
            .id(getProcessorId(flow, node, sink, "toNode"));
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        LOG.debug("Adding routes: " + sink);
        super.addRoutesToCamelContext(context);
    }

    public void startRoutes() throws Exception {
        log.debug("Starting routes: " + sink);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().startRoute(routeDefinition.getId());
        }
    }

    public void removeRoutesFromCamelContext() throws Exception {
        LOG.debug("Removing routes: " + sink);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().removeRouteDefinition(routeDefinition);
        }
    }
}
