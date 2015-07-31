package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.beta.server.route.FlowRouteManager.DESTINATION_SINK_ID;
import static org.openremote.beta.server.route.RouteManagementUtil.*;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public abstract class NodeRouteManager extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRouteManager.class);

    protected final Flow flow;
    protected final Node node;
    protected List<SinkRouteManager> sinkRouteManagers = new ArrayList<>();

    public NodeRouteManager(CamelContext context, Flow flow, Node node) {
        super(context);
        this.flow = flow;
        this.node = node;
        LOG.debug("Creating builder for: " + node);
        for (Slot slot : node.findSlots(Slot.TYPE_SINK)) {
            sinkRouteManagers.add(new SinkRouteManager(getContext(), flow, node, slot));
        }
    }

    public Flow getFlow() {
        return flow;
    }

    public Node getNode() {
        return node;
    }

    public String getDestinationSinkId(Exchange exchange) {
        return exchange.getIn().getHeader(DESTINATION_SINK_ID, String.class);
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + node);

        RouteDefinition routeDefinition = from("direct:" + node.getIdentifier().getId())
            .routeId(getRouteId(flow, node))
            .routeDescription(getRouteDescription(flow, node))
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, "Node processing: " + getRouteDescription(flow, node));

        // Optional sending exchange to an endpoint before node processing
        if (node.hasProperties()) {
            String preEndpoint = getString(getMap(node.getProperties()), "preEndpoint");
            if (preEndpoint != null) {
                routeDefinition.to(preEndpoint)
                    .id(getProcessorId(flow, node, "preEndpoint"));
            }
        }

        // The processing of the node
        configure(routeDefinition);

        // Optional sending exchange to an endpoint after processing
        if (node.hasProperties()) {
            String postEndpoint = getString(getMap(node.getProperties()), "postEndpoint");
            if (postEndpoint != null) {
                routeDefinition.to(postEndpoint)
                    .id(getProcessorId(flow, node, "postEndpoint"));
            }
        }

        // Send the exchange through the wires to the next node(s)
        routeDefinition.bean(method(new WiringRouter(flow, node)))
            .id(getProcessorId(flow, node, "toWires"));
    }

    protected abstract void configure(RouteDefinition routeDefinition) throws Exception;

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        LOG.debug("Adding routes: " + flow);
        super.addRoutesToCamelContext(context);
        for (SinkRouteManager sinkRouteManager : sinkRouteManagers) {
            sinkRouteManager.addRoutesToCamelContext(sinkRouteManager.getContext());
        }
    }

    public void startRoutes() throws Exception {
        log.debug("Starting routes: " + node);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().startRoute(routeDefinition.getId());
        }
        for (SinkRouteManager sinkRouteManager : sinkRouteManagers) {
            sinkRouteManager.startRoutes();
        }
    }

    public void removeRoutesFromCamelContext() throws Exception {
        LOG.debug("Removing routes: " + node);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().removeRouteDefinition(routeDefinition);
        }
        for (SinkRouteManager sinkRouteManager : sinkRouteManagers) {
            sinkRouteManager.removeRoutesFromCamelContext();
        }
    }
}
