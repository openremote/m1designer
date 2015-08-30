package org.openremote.beta.server.route;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.event.EventService;
import org.openremote.beta.server.route.predicate.NodePropertyIsTrue;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.openremote.beta.server.route.RouteConstants.*;
import static org.openremote.beta.server.route.SubflowRoute.copyCorrelationStack;
import static org.openremote.beta.server.util.JsonUtil.JSON;

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
    protected final ObjectNode nodeProperties;
    protected final ProducerTemplate producerTemplate;

    public NodeRoute(CamelContext context, Flow flow, Node node) {
        super(context);
        this.flow = flow;
        this.node = node;
        if (node.getProperties() != null) {
            try {
                this.nodeProperties = JSON.readValue(node.getProperties(), ObjectNode.class);
            } catch (IOException ex) {
                throw new RuntimeException("Error reading properties of '" + node.getIdentifier() + "'", ex);
            }
        } else {
            nodeProperties = JSON.createObjectNode();
        }
        this.producerTemplate = getContext().createProducerTemplate();
    }

    public Flow getFlow() {
        return flow;
    }

    public Node getNode() {
        return node;
    }

    /**
     * http://wiki.fasterxml.com/JacksonTreeModel
     */
    public ObjectNode getNodeProperties() {
        return nodeProperties;
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

    public String getSlotId(Exchange exchange) {
        return exchange.getIn().getHeader(SLOT_ID, String.class);
    }

    public String getInstanceId(Exchange exchange) {
        return exchange.getIn().getHeader(INSTANCE_ID, String.class);
    }

    public Predicate isSinkRouting() {
        return exchange -> exchange.getProperty(SINK_ROUTING, false, Boolean.class);
    }

    public Predicate isNodePropertyTrue(String propertyPath) {
        return new NodePropertyIsTrue(getNode(), getNodeProperties(), propertyPath);
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + getNode());

        RouteDefinition routeDefinition = from("direct:" + node.getId())
            .routeId(getRouteId())
            .routeDescription(getRouteDescription())
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, ">>> " + getRouteDescription() + " starts processing: ${body}");

        routeDefinition
            .process(exchange -> {
                Slot slot = getNode().findSlot(getSlotId(exchange));
                if (slot == null) {
                    LOG.debug("Slot '" + getSlotId(exchange) + "' not found on node, stopping exchange: " + getNode());
                    exchange.setProperty(Exchange.ROUTE_STOP, true);
                } else {
                    // Route the message to either:
                    // - Sink slot: Apply node processors, then get source slots and forward to attached wires
                    // - Source slot: Directly get source slot and forward to attached wires
                    if (slot.isOfType(Slot.TYPE_SINK)) {
                        exchange.setProperty(SINK_ROUTING, true);
                    }
                }
            }).id(getProcessorId("checkDestinationSlot"));

        routeDefinition
            .process(exchange -> {
                // For stateful nodes, we need to know which instance this message is for. This can either
                // be the node instance if we are not called within a flow (e.g. directly in tests). Usually it is
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

        ChoiceDefinition sinkProcessingDefinition =
            routeDefinition
                .choice()
                .id(getProcessorId("isSinkRouting"))
                .when(isSinkRouting())
                .log(LoggingLevel.DEBUG, LOG, "# Sink routing, node is processing: ${body}");

        // ###################### SINK ROUTING BEGIN ######################

        // Optional sending message event to clients
        if (isPublishingMessageEvents()) {
            sinkProcessingDefinition
                .process(exchange -> {
                    LOG.debug("Sending message event to clients: " + getNode());
                    Slot slot = getNode().findSlot(getSlotId(exchange));
                    Map<String, Object> headers = new HashMap<>(exchange.getIn().getHeaders());
                    // Cleanup the copy of the map
                    headers.remove(RouteConstants.SLOT_ID);
                    headers.remove(RouteConstants.INSTANCE_ID);
                    String body = exchange.getIn().getBody(String.class);
                    getContext().hasService(EventService.class).sendMessageEvent(
                        getNode(), slot, body, headers
                    );
                })
                .id(getProcessorId("toClients"));
        }

        // Optional sending exchange to an endpoint before node processing
        if (getNode().getPreEndpoint() != null) {
            sinkProcessingDefinition.to(getNode().getPreEndpoint())
                .id(getProcessorId("preEndpoint"));
        }

        // The processing of the node
        configureProcessing(sinkProcessingDefinition);

        // Optional sending exchange to an endpoint after processing
        if (getNode().getPostEndpoint() != null) {
            sinkProcessingDefinition.to(getNode().getPostEndpoint())
                .id(getProcessorId("postEndpoint"));
        }

        sinkProcessingDefinition
            .log(LoggingLevel.DEBUG, LOG, "<<< " + getRouteDescription() + " done processing: ${body}");

        // If this is a sink slot that's mapped to a node property, we don't continue routing the
        // message to destination wires. Later we might implement servers-side dynamic properties
        // and pick a target source slot dynamically (depending on which property changed) but
        // now we simply stop here.
        sinkProcessingDefinition
            .process(exchange -> {
                    Slot slot = getNode().findSlot(getSlotId(exchange));
                    if (slot.getPropertyPath() != null) {
                        LOG.debug("Sink slot '" + getSlotId(exchange) + "' maps to a node property path, stopping exchange: " + getNode());
                        exchange.setProperty(Exchange.ROUTE_STOP, true);
                    }
                }
            ).id(getProcessorId("checkSlotPropertyPath"));

        // ###################### SINK ROUTING END ######################

        ProcessorDefinition finalizeDefinition =
            sinkProcessingDefinition
                .otherwise()
                .log(LoggingLevel.DEBUG, LOG, "# Source routing, node is forwarding : ${body}")
                .end();

        finalizeDefinition.removeHeader(INSTANCE_ID)
            .id(getProcessorId("removeInstanceId"));

        // Send the cleaned exchange through the wires to the next node(s)
        configureDestination(finalizeDefinition);
    }

    // This can then be selectively enabled in the event service with clientAccess property
    // on the node, so we don't spam the client event bus.
    protected boolean isPublishingMessageEvents() {
        return true;
    }

    protected void configureDestination(ProcessorDefinition routeDefinition) throws Exception {
        routeDefinition
            .process(exchange -> {
                Slot[] sourceSlots = new Slot[0];
                if (isSinkRouting().matches(exchange)) {
                    // Send message to all source slots
                    sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                } else {
                    // Send message to a single source slot
                    sourceSlots = new Slot[]{getNode().findSlot(getSlotId(exchange))};
                }
                exchange.getIn().removeHeader(SLOT_ID);
                LOG.debug("Using wires of destination source slots: " + Arrays.toString(sourceSlots));

                // Find source slots and get the destination node and sink by examining the wires
                for (Slot sourceSlot : sourceSlots) {
                    LOG.debug("Finding wires attached to: " + sourceSlot);
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getId());
                    LOG.debug("Found wires, sending exchange copy to each: " + Arrays.toString(sourceWires));
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
        exchangeCopy.getIn().setHeader(SLOT_ID, destinationSinkId);
        getProducerTemplate().send("direct:" + destinationNodeId, exchangeCopy);
    }

    protected Exchange copyExchange(Exchange exchange, boolean popStack) {
        Exchange exchangeCopy = exchange.copy();
        exchangeCopy.getIn().setHeaders(copyCorrelationStack(exchangeCopy.getIn().getHeaders(), popStack));
        return exchangeCopy;
    }

    protected abstract void configureProcessing(ProcessorDefinition routeDefinition) throws Exception;

}
