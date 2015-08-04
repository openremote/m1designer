package org.openremote.beta.server.route;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

import static org.openremote.beta.server.route.RouteConstants.*;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public abstract class NodeRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRoute.class);

    public static String getRouteId(Flow flow, Node node) {
        return node.getIdentifier() + ";" + flow.getIdentifier();
    }

    public static String getRouteId(Flow flow, Node node, Slot slot) {
        return slot.getIdentifier() + ";" + node.getIdentifier() + ";" + flow.getIdentifier();
    }

    public static String getProcessorId(Flow flow, Node node, String processorLabel) {
        return processorLabel + ";" + node.getIdentifier() + ";" + flow.getIdentifier();
    }

    public static String getProcessorId(Flow flow, Node node, Slot slot, String processorLabel) {
        return processorLabel + ";" + slot.getIdentifier() + ";" + node.getIdentifier() + ";" + flow.getIdentifier();
    }

    public static String getRouteDescription(Flow flow, Node node) {
        return node + ";" + flow;
    }

    protected final Flow flow;
    protected final Node node;
    protected final ProducerTemplate producerTemplate;

    public NodeRoute(CamelContext context, Flow flow, Node node) {
        super(context);
        this.flow = flow;
        this.node = node;
        this.producerTemplate = getContext().createProducerTemplate();
    }

    public Flow getFlow() {
        return flow;
    }

    public Node getNode() {
        return node;
    }

    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    public String getDestinationSinkId(Exchange exchange) {
        return exchange.getIn().getHeader(DESTINATION_SINK_ID, String.class);
    }

    public String getInstanceId(Exchange exchange) {
        return exchange.getIn().getHeader(NODE_INSTANCE_ID, String.class);
    }

    public String getPropertyValue(String property) {
        if (!getNode().hasProperties())
            return null;
        return getString(getMap(node.getProperties()), property);
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + node);

        RouteDefinition routeDefinition = from("direct:" + node.getIdentifier().getId())
            .routeId(getRouteId(flow, node))
            .routeDescription(getRouteDescription(flow, node))
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, ">>> " + getRouteDescription(flow, node) + " starts processing: ${body}");

        routeDefinition
            .process(exchange -> {
                // If we received a message and it doesn't have a destination sink, use this node's first sink slot
                String destinationSinkId = getDestinationSinkId(exchange);
                if (destinationSinkId != null)
                    return;

                Slot[] sinks = getNode().findSlots(Slot.TYPE_SINK);
                if (sinks.length == 0)
                    return;
                exchange.getIn().setHeader(DESTINATION_SINK_ID, sinks[0].getIdentifier().getId());
            }).id(getProcessorId(flow, node, "setDestinationSink"));

        routeDefinition
            .process(exchange -> {
                // For stateful nodes, we need to know which instance this message is for. This can either
                // be the node instance if we are not called within a subflow. Or it is the bottom of the
                // subflow call stack, the "outermost" subflow if they are nested.
                Stack<String> correlationStack = exchange.getIn().getHeader(SUBFLOW_CORRELATION_STACK, Stack.class);
                if (correlationStack != null && correlationStack.size() > 0) {
                    // Use the bottom of the stack, the "outermost" subflow is our instance
                    String correlationId = correlationStack.get(0);
                    LOG.debug("Message received for instance at bottom of correlation stack: " + correlationId);
                    exchange.getIn().setHeader(NODE_INSTANCE_ID, correlationId);
                } else {
                    exchange.getIn().setHeader(NODE_INSTANCE_ID, getNode().getIdentifier().getId());
                }
            }).id(getProcessorId(flow, node, "setInstanceId"));

        // Optional sending exchange to an endpoint before node processing
        if (node.hasProperties()) {
            String preEndpoint = getPropertyValue("preEndpoint");
            if (preEndpoint != null) {
                routeDefinition.to(preEndpoint)
                    .id(getProcessorId(flow, node, "preEndpoint"));
            }
        }

        // The processing of the node
        configureProcessing(routeDefinition);

        // Optional sending exchange to an endpoint after processing
        if (node.hasProperties()) {
            String postEndpoint = getPropertyValue("postEndpoint");
            if (postEndpoint != null) {
                routeDefinition.to(postEndpoint)
                    .id(getProcessorId(flow, node, "postEndpoint"));
            }
        }

        routeDefinition.removeHeader(NODE_INSTANCE_ID)
            .id(getProcessorId(flow, node, "removeInstanceId"));

        routeDefinition.removeHeader(DESTINATION_SINK_ID)
            .id(getProcessorId(flow, node, "removeDestinationSink"));

        routeDefinition
            .log(LoggingLevel.DEBUG, LOG, "<<< " + getRouteDescription(flow, node) + " done processing: ${body}");

        // Send the exchange through the wires to the next node(s)
        configureDestination(routeDefinition);
    }

    protected void configureDestination(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .process(exchange -> {
                Slot[] sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                // Find source slots and get the destination node and sink by examining the wires
                for (Slot sourceSlot : sourceSlots) {
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getIdentifier().getId());
                    for (Wire sourceWire : sourceWires) {
                        sendExchangeCopy(sourceWire.getSinkId(), exchange, false);
                    }
                }
            })
            .id(getProcessorId(getFlow(), getNode(), "toWires"))
            .stop()
            .id(getProcessorId(getFlow(), getNode(), "stopNodeRoute"))
        ;
    }

    protected Node getOwningNodeOfSlot(String slotId) {
        return getContext().hasService(RouteManagementService.class).getRunningNodeOwnerOfSlot(slotId);
    }

    protected void sendExchangeCopy(String destinationSinkId, Exchange exchange, boolean popStack) throws Exception {
        Node destinationNode = getOwningNodeOfSlot(destinationSinkId);
        if (destinationNode == null) {
            LOG.debug("Destination node owning sink '" + destinationSinkId + "' not found in running flows, skipping");
            return;
        }
        String destinationNodeId = destinationNode.getIdentifier().getId();

        LOG.debug("Sending copy of exchange to node '" + destinationNodeId + "' sink: " + destinationSinkId);
        Exchange exchangeCopy = copyExchange(exchange, popStack);
        exchangeCopy.getIn().setHeader(DESTINATION_SINK_ID, destinationSinkId);
        getProducerTemplate().send("direct:" + destinationNodeId, exchangeCopy);
    }

    protected Exchange copyExchange(Exchange exchange, boolean popStack) {
        Exchange exchangeCopy = exchange.copy();
        Stack<String> subflowCorrelationStack = exchange.getIn().getHeader(RouteConstants.SUBFLOW_CORRELATION_STACK, Stack.class);
        if (subflowCorrelationStack != null) {
            Stack<String> subflowCorrelationStackCopy = (Stack<String>) subflowCorrelationStack.clone();
            if (subflowCorrelationStack.size() > 0 && popStack) {
                subflowCorrelationStackCopy.pop();
            }
            exchangeCopy.getIn().setHeader(RouteConstants.SUBFLOW_CORRELATION_STACK, subflowCorrelationStackCopy);
        }
        return exchangeCopy;
    }

    protected abstract void configureProcessing(RouteDefinition routeDefinition) throws Exception;

}
