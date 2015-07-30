package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinkRouteManager extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SinkRouteManager.class);

    protected final Flow flow;
    protected final Node node;
    protected final Slot sink;

    public SinkRouteManager(CamelContext context, Flow flow, Node node, Slot sink) {
        super(context);
        this.flow = flow;
        this.node = node;
        this.sink = sink;
        LOG.debug("Creating builder for: " + sink);
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + sink);

        from("direct:" + sink.getId())
            .routeId(flow.toString() + "###" + node.toString() + "###" + sink.toString())
            .autoStartup(false)
            .setHeader(FlowRouteManager.DESTINATION_SINK_ID, constant(sink.getId()))
            .id(flow.toString() + "###" + node.toString() + "###" + sink.toString() + "###setDestinationSink")
            .to("direct:" + node.getId())
            .id(flow.toString() + "###" + node.toString() + "###" + sink.toString() + "###toNode");
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
