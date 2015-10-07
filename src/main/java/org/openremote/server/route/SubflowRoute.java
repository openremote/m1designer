package org.openremote.server.route;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.catalog.WidgetNodeDescriptor;
import org.openremote.shared.flow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SubflowRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(SubflowRoute.class);

    public static final String WIDGET_COMPONENT = "or-console-widget-composite";

    public static class Descriptor extends WidgetNodeDescriptor {

        @Override
        public NodeColor getColor() {
            return NodeColor.VIRTUAL;
        }

        @Override
        public boolean isInternal() {
            return true;
        }

        @Override
        public String getType() {
            return Node.TYPE_SUBFLOW;
        }

        @Override
        public String getTypeLabel() {
            return Node.TYPE_SUBFLOW_LABEL;
        }

        @Override
        protected ObjectNode getInitialProperties() {
            return WIDGET_INITIAL_PROPERTIES.deepCopy()
                .put(PROPERTY_COMPONENT, WIDGET_COMPONENT);
        }

        @Override
        public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
            return new SubflowRoute(context, flow, node);
        }
    }

    private static final String SUBFLOW_CORRELATION_STACK = "SUBFLOW_CORRELATION_STACK";

    public static void pushOntoCorrelationStack(Map<String, Object> headers, String identifier) {
        // Create or get existing stack from headers
        @SuppressWarnings("unchecked")
        Stack<String> subflowCorrelationStack = hasCorrelationStack(headers)
            ? (Stack<String>) headers.get(SUBFLOW_CORRELATION_STACK)
            : new Stack<>();

        LOG.debug("Pushing identifier'" + identifier + "' onto correlation stack: " + subflowCorrelationStack);
        subflowCorrelationStack.push(identifier);
        LOG.debug("Setting correlation stack header: " + subflowCorrelationStack);
        headers.put(SUBFLOW_CORRELATION_STACK, subflowCorrelationStack);
    }

    public static String peekCorrelationStack(Map<String, Object> headers, boolean root, boolean removeHeader) {
        String instanceId = null;
        if (hasCorrelationStack(headers)) {

            @SuppressWarnings("unchecked")
            Stack<String> correlationStack = (Stack<String>) headers.get(SUBFLOW_CORRELATION_STACK);

            if (correlationStack.size() > 0) {
                if (root) {
                    instanceId = correlationStack.get(0);
                    LOG.debug("Got correlation stack root element: " + instanceId);
                } else {
                    instanceId = correlationStack.peek();
                    LOG.debug("Got correlation stack current element: " + instanceId);
                }
            }

            if (removeHeader)
                headers.remove(SUBFLOW_CORRELATION_STACK);
        }
        return instanceId;
    }

    public static Map<String, Object> copyCorrelationStack(Map<String, Object> headers, boolean popStack) {
        Map<String, Object> headersCopy = new HashMap<>(headers);
        if (hasCorrelationStack(headers)) {

            @SuppressWarnings("unchecked")
            Stack<String> correlationStack = (Stack<String>) headers.get(SUBFLOW_CORRELATION_STACK);

            @SuppressWarnings("unchecked") Stack<String> correlationStackCopy = (Stack<String>) correlationStack.clone();

            if (correlationStackCopy.size() > 0 && popStack) {
                correlationStackCopy.pop();
            }
            headersCopy.put(SUBFLOW_CORRELATION_STACK, correlationStackCopy);
        }
        return headersCopy;
    }

    public static boolean hasCorrelationStack(Map<String, Object> headers) {
        return headers.containsKey(SUBFLOW_CORRELATION_STACK);
    }

    public SubflowRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        routeDefinition
            .process(exchange -> {
                LOG.debug("Processing exchange for subflow: " + getNode());
                String destinationSinkId = getSlotId(exchange);
                Slot destinationSink = getNode().findSlot(destinationSinkId);
                LOG.debug("Found destination sink: " + destinationSink);
                if (destinationSink.getPeerId() != null) {

                    LOG.debug("Found destination peer sink: " + destinationSink.getPeerId());

                    pushOntoCorrelationStack(exchange.getIn().getHeaders(), getNode().getId());

                    sendExchangeCopy(destinationSink.getPeerId(), exchange, false);

                } else {
                    LOG.debug("No peer for destination sink slot, stopping exchange: " + destinationSink);
                }
            })
            .id(getProcessorId("toSubflowPeer"))
            .log(LoggingLevel.DEBUG, LOG, "<<< Subflow " + getRouteDescription() + " done processing: '${body}'")
            .stop()
            .id(getProcessorId("stopSubflow"));

        LOG.debug("Handling subflow source slots: " + getNode());

        for (Slot sourceSlot : getNode().findSlots(Slot.TYPE_SOURCE)) {

            LOG.debug("Handling subflow source slot: " + sourceSlot);
            if (sourceSlot.getPeerId() == null) {
                LOG.debug("No peer for source slot, not registering any asynchronous consumer: " + sourceSlot);
                continue;
            }

            LOG.debug("Consuming from source peer asynchronous queue: " + sourceSlot.getPeerId());
            from("seda:" + sourceSlot.getPeerId() + "?multipleConsumers=true")
                .routeId(getRouteId(sourceSlot))
                .process(exchange -> {
                    LOG.debug("Received message from asynchronous queue: " + sourceSlot.getPeerId());

                    if (!hasCorrelationStack(exchange.getIn().getHeaders())) {
                        LOG.warn("No correlation stack in message from asynchronous queue, dropping here: " + getNode());
                        return;
                    }

                    String currentInstanceId = peekCorrelationStack(exchange.getIn().getHeaders(), false, false);

                    if (getNode().getId().equals(currentInstanceId)) {

                        LOG.debug("Correlation found for '" + getNode() + "', finding wires of: " + sourceSlot);

                        Wire[] sourceWires = getFlow().findWiresForSource(sourceSlot.getId());
                        for (Wire sourceWire : sourceWires) {
                            sendExchangeCopy(sourceWire.getSinkId(), exchange, true);
                        }

                    } else {
                        LOG.debug("No correlation found for received message on: " + getNode());
                    }
                })
                .id(getProcessorId(sourceSlot, "toSubflowWires"));

        }
    }

}
