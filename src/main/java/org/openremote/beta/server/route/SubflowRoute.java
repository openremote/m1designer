package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class SubflowRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(SubflowRoute.class);

    public SubflowRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {

        routeDefinition
            .process(exchange -> {
                LOG.debug("Processing exchange for subflow: " + getNode());
                String destinationSinkId = getDestinationSinkId(exchange);
                Slot destinationSink = getNode().findSlot(destinationSinkId);
                LOG.debug("Found destination sink: " + destinationSink);
                if (destinationSink.getPeerIdentifier() != null) {
                    LOG.debug("Found destination peer sink: " + destinationSink.getPeerIdentifier());

                    Stack<String> subflowCorrelationStack = exchange.getIn().getHeader(RouteConstants.SUBFLOW_CORRELATION_STACK, new Stack<>(), Stack.class);
                    LOG.debug("Pushing subflow '" + node.getIdentifier().getId() + "' onto correlation stack: " + subflowCorrelationStack);
                    subflowCorrelationStack.push(node.getIdentifier().getId());
                    LOG.debug("Setting correlation stack header: " + subflowCorrelationStack);
                    exchange.getIn().setHeader(RouteConstants.SUBFLOW_CORRELATION_STACK, subflowCorrelationStack);

                    sendExchangeCopy(destinationSink.getPeerIdentifier().getId(), exchange, false);

                } else {
                    LOG.debug("No peer for destination sink slot, stopping exchange: " + destinationSink);
                }
            })
            .id(getProcessorId(flow, node, "toSubflowPeer"))
            .stop()
            .id(getProcessorId(flow, node, "stopSubflow"));

        LOG.debug("Handling subflow source slots: " + node);

        for (Slot sourceSlot : getNode().findSlots(Slot.TYPE_SOURCE)) {

            LOG.debug("Handling subflow source slot: " + sourceSlot);
            if (sourceSlot.getPeerIdentifier() == null) {
                LOG.debug("No peer for source slot, not registering any asynchronous consumer: " + sourceSlot);
                continue;
            }

            LOG.debug("Consuming from source peer asynchronous queue: " + sourceSlot.getPeerIdentifier().getId());
            from("seda:" + sourceSlot.getPeerIdentifier().getId() + "?multipleConsumers=true")
                .routeId(getRouteId(flow, node, sourceSlot))
                .process(exchange -> {
                    LOG.debug("Received message from asynchronous queue: " + sourceSlot.getPeerIdentifier().getId());

                    Stack<String> subflowCorrelationStack = exchange.getIn().getHeader(RouteConstants.SUBFLOW_CORRELATION_STACK, Stack.class);
                    LOG.debug("Testing subflow '" + node + "' correlation stack: " + subflowCorrelationStack);
                    if (subflowCorrelationStack == null) {
                        LOG.warn("No correlation stack in message from asynchronous queue, dropping here: " + node);
                        return;
                    }
                    if (subflowCorrelationStack.peek().equals(node.getIdentifier().getId())) {
                        LOG.debug("Correlation found for '" + node + "', finding wires of: " + sourceSlot);
                        Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getIdentifier().getId());
                        for (Wire sourceWire : sourceWires) {
                            sendExchangeCopy(sourceWire.getSinkId(), exchange, true);
                        }
                    } else {
                        LOG.debug("No correlation found for '" + node + "' in stack: " + subflowCorrelationStack);
                    }
                })
                .id(getProcessorId(flow, node, sourceSlot, "toSubflowWires"));

        }
    }

    @Override
    protected void configureDestination(RouteDefinition routeDefinition) throws Exception {
        // Do nothing, internal wiring of queues!
    }
}
