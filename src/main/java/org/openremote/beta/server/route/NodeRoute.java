package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.event.EventService;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.openremote.beta.server.route.RouteConstants.INSTANCE_ID;
import static org.openremote.beta.server.route.RouteConstants.SINK_SLOT_ID;
import static org.openremote.beta.server.route.SubflowRoute.copyCorrelationStack;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public abstract class NodeRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRoute.class);

    public static String getConsumerUri(Node node) {
        return getConsumerUri(node.getId());
    }

    public static String getConsumerUri(String nodeId) {
        return "direct:" + nodeId;
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

    public String getRouteId() {
        return getNode().getIdentifier() + ";" + getFlow().getIdentifier();
    }

    public String getRouteId(Slot slot) {
        return slot.getIdentifier() + ";" + getNode().getIdentifier() + ";" + getFlow().getIdentifier();
    }

    public String getProcessorId(String processorLabel) {
        return processorLabel + ";" + getNode().getIdentifier() + ";" + getFlow().getIdentifier();
    }

    public String getProcessorId(Slot slot, String processorLabel) {
        return processorLabel + ";" + slot.getIdentifier() + ";" + getNode().getIdentifier() + ";" + getFlow().getIdentifier();
    }

    public String getRouteDescription() {
        return getNode() + ";" + getFlow();
    }

    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    public String getSinkSlotId(Exchange exchange) {
        return exchange.getIn().getHeader(SINK_SLOT_ID, String.class);
    }

    public String getInstanceId(Exchange exchange) {
        return exchange.getIn().getHeader(INSTANCE_ID, String.class);
    }

    public String getPropertyValue(String property) {
        if (!getNode().hasProperties())
            return null;
        return getString(getMap(node.getProperties()), property);
    }

    @Override
    public void configure() throws Exception {
        if (!isServerRoutingEnabled()) {
            LOG.debug("Node is not routing, skipping: " + getNode());
        }
        LOG.debug("Configure routes: " + getNode());

        RouteDefinition routeDefinition = from("direct:" + node.getId())
            .routeId(getRouteId())
            .routeDescription(getRouteDescription())
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, ">>> " + getRouteDescription() + " starts processing: ${body}");

        routeDefinition
            .process(exchange -> {
                // If we received a message and it doesn't have a destination sink, use this node's first sink slot
                String destinationSinkId = getSinkSlotId(exchange);
                if (destinationSinkId != null)
                    return;

                Slot[] sinks = getNode().findSlots(Slot.TYPE_SINK);
                if (sinks.length == 0)
                    return;
                exchange.getIn().setHeader(SINK_SLOT_ID, sinks[0].getId());
            }).id(getProcessorId("setDestinationSink"));

        routeDefinition
            .process(exchange -> {
                // For stateful nodes, we need to know which instance this message is for. This can either
                // be the node instance if we are not called within a flow (directly in tests?). Usually it is
                // the first element of the flow call stack, the "outermost" flow if they are nested.
                String instanceId = SubflowRoute.peekCorrelationStack(exchange.getIn().getHeaders(), true, false);
                if (instanceId != null) {
                    LOG.debug("Message received for instance (root of correlation stack): " + instanceId);
                } else {
                    instanceId = getNode().getId();
                    LOG.debug("Message received for instance (this): " + instanceId);
                }
                exchange.getIn().setHeader(INSTANCE_ID, instanceId);
            }).id(getProcessorId("setInstanceId"));

        // Optional sending message event to clients
        if (isPublishingMessageEvents()) {
            routeDefinition
                .process(exchange -> {
                    LOG.debug("Sending message event to clients: " + getNode());
                    Slot sink = getNode().findSlot(getSinkSlotId(exchange));
                    Map<String, Object> headers = new HashMap<>(exchange.getIn().getHeaders());
                    // Cleanup the copy of the map
                    headers.remove(RouteConstants.SINK_SLOT_ID);
                    headers.remove(RouteConstants.INSTANCE_ID);
                    String body = exchange.getIn().getBody(String.class);
                    getContext().hasService(EventService.class).sendMessageEvent(
                        getFlow(), getNode(), sink, body, headers
                    );
                })
                .id(getProcessorId("toClients"));
        }

        // Optional sending exchange to an endpoint before node processing
        if (node.hasProperties()) {
            String preEndpoint = getPropertyValue("preEndpoint");
            if (preEndpoint != null) {
                routeDefinition.to(preEndpoint)
                    .id(getProcessorId("preEndpoint"));
            }
        }

        // The processing of the node
        configureProcessing(routeDefinition);

        // Optional sending exchange to an endpoint after processing
        if (node.hasProperties()) {
            String postEndpoint = getPropertyValue("postEndpoint");
            if (postEndpoint != null) {
                routeDefinition.to(postEndpoint)
                    .id(getProcessorId("postEndpoint"));
            }
        }

        routeDefinition.removeHeader(INSTANCE_ID)
            .id(getProcessorId("removeInstanceId"));

        routeDefinition.removeHeader(SINK_SLOT_ID)
            .id(getProcessorId("removeDestinationSink"));

        routeDefinition
            .log(LoggingLevel.DEBUG, LOG, "<<< " + getRouteDescription() + " done processing: ${body}");

        // Send the exchange through the wires to the next node(s)
        configureDestination(routeDefinition);
    }

    protected boolean isServerRoutingEnabled() {
        return true;
    }

    // Generally all nodes which are stateful should probably publish message events.
    // This can then be selectively enabled in the event service with clientAccess property
    // on the node, so we don't spam the client event bus.
    protected boolean isPublishingMessageEvents() {
        return false;
    }

    protected void configureDestination(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .process(exchange -> {
                Slot[] sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                // Find source slots and get the destination node and sink by examining the wires
                for (Slot sourceSlot : sourceSlots) {
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getId());
                    for (Wire sourceWire : sourceWires) {
                        sendExchangeCopy(sourceWire.getSinkId(), exchange, false);
                    }
                }
            })
            .id(getProcessorId("toWires"))
            .stop()
            .id(getProcessorId("stopNodeRoute"))
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
        String destinationNodeId = destinationNode.getId();

        LOG.debug("Sending copy of exchange to node '" + destinationNodeId + "' sink: " + destinationSinkId);
        Exchange exchangeCopy = copyExchange(exchange, popStack);
        exchangeCopy.getIn().setHeader(SINK_SLOT_ID, destinationSinkId);
        getProducerTemplate().send("direct:" + destinationNodeId, exchangeCopy);
    }

    protected Exchange copyExchange(Exchange exchange, boolean popStack) {
        Exchange exchangeCopy = exchange.copy();
        exchangeCopy.getIn().setHeaders(copyCorrelationStack(exchangeCopy.getIn().getHeaders(), popStack));
        return exchangeCopy;
    }

    protected abstract void configureProcessing(RouteDefinition routeDefinition) throws Exception;

}
