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
import java.util.Stack;

import static org.openremote.beta.server.route.FlowRoute.DESTINATION_SINK_ID;
import static org.openremote.beta.server.route.RouteManagementUtil.*;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public abstract class NodeRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRoute.class);

    public static final String NODE_INSTANCE_ID = "NODE_INSTANCE_ID";

    protected final Flow flow;
    protected final Node node;
    protected List<SinkRoute> sinkRoutes = new ArrayList<>();

    public NodeRoute(CamelContext context, Flow flow, Node node) {
        super(context);
        this.flow = flow;
        this.node = node;
        LOG.debug("Creating builder for: " + node);
        for (Slot slot : node.findSlots(Slot.TYPE_SINK)) {
            sinkRoutes.add(new SinkRoute(getContext(), flow, node, slot));
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

    public String getInstanceId(Exchange exchange) {
        return exchange.getIn().getHeader(NODE_INSTANCE_ID, String.class);
    }

    public String getNodeSinkId(int position) {
        Slot sink = node.findSlotByPosition(position, Slot.TYPE_SINK);
        return sink != null ? sink.getIdentifier().getId() : null;
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + node);

        RouteDefinition routeDefinition = from("direct:" + node.getIdentifier().getId())
            .routeId(getRouteId(flow, node))
            .routeDescription(getRouteDescription(flow, node))
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, "Node processing: " + getRouteDescription(flow, node));

        routeDefinition
            .process(exchange -> {
                // For stateful nodes, we need to know which instance this message is for. This can either
                // be the node instance if we are not called within a subflow. Or it is the bottom of the
                // subflow call stack, the "outermost" subflow if they are nested.
                Stack<String> correlationStack = exchange.getIn().getHeader(SubflowRoute.SUBFLOW_CORRELATION_STACK, Stack.class);
                if (correlationStack != null && correlationStack.size() > 0) {
                    // Use the bottom of the stack, the "outermost" subflow is our instance
                    String correlationId = correlationStack.get(0);
                    LOG.debug("Message received for instance at bottom of correlation stack: " + correlationId);
                    exchange.getIn().setHeader(NODE_INSTANCE_ID, correlationId);
                } else {
                    log.debug("Message received for this node instance: " + getNode());
                    exchange.getIn().setHeader(NODE_INSTANCE_ID, getNode().getIdentifier().getId());
                }
            }).id(getProcessorId(flow, node, "setInstanceId"));

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

        routeDefinition.removeHeader(DESTINATION_SINK_ID)
            .id(getProcessorId(flow, node, "removeDestinationSink"));

        routeDefinition.removeHeader(NODE_INSTANCE_ID)
            .id(getProcessorId(flow, node, "removeInstanceId"));

        // Send the exchange through the wires to the next node(s)
        routeDefinition.bean(method(new WiringRouter(flow, node)))
            .id(getProcessorId(flow, node, "toWires"));
    }

    protected abstract void configure(RouteDefinition routeDefinition) throws Exception;

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        LOG.debug("Adding routes: " + flow);
        super.addRoutesToCamelContext(context);
        for (SinkRoute sinkRoute : sinkRoutes) {
            sinkRoute.addRoutesToCamelContext(sinkRoute.getContext());
        }
    }

    public void startRoutes() throws Exception {
        log.debug("Starting routes: " + node);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().startRoute(routeDefinition.getId());
        }
        for (SinkRoute sinkRoute : sinkRoutes) {
            sinkRoute.startRoutes();
        }
    }

    public void removeRoutesFromCamelContext() throws Exception {
        LOG.debug("Removing routes: " + node);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().removeRouteDefinition(routeDefinition);
        }
        for (SinkRoute sinkRoute : sinkRoutes) {
            sinkRoute.removeRoutesFromCamelContext();
        }
    }
}
