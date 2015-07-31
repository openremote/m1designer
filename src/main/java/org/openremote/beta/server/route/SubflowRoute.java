package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

import static org.openremote.beta.server.route.RouteManagementUtil.getProcessorId;
import static org.openremote.beta.server.route.RouteManagementUtil.getRouteId;

public class SubflowRoute extends NodeRouteManager {

    private static final Logger LOG = LoggerFactory.getLogger(SubflowRoute.class);

    public static final String SUBFLOW_CORRELATION_STACK = "SUBFLOW_CORRELATION_STACK";
    public static final String PEER_SINK_ID = "PEER_SINK_ID";

    public SubflowRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configure(RouteDefinition routeDefinition) throws Exception {

        routeDefinition
            .process(exchange -> {
                LOG.debug("Processing exchange for subflow: " + getNode());
                String destinationSinkId = getDestinationSinkId(exchange);
                Slot destinationSink = getNode().findSlot(destinationSinkId);
                LOG.debug("Found destination sink: " + destinationSink);
                if (destinationSink.getPeerIdentifier() != null) {
                    LOG.debug("Found destination peer sink: " + destinationSink.getPeerIdentifier());
                    exchange.getIn().setHeader(PEER_SINK_ID, destinationSink.getPeerIdentifier().getId());

                    Stack<String> subflowCorrelationStack = exchange.getIn().getHeader(SUBFLOW_CORRELATION_STACK, new Stack<>(), Stack.class);
                    LOG.debug("Pushing subflow '" + node.getIdentifier().getId() + "' onto correlation stack: " + subflowCorrelationStack);
                    subflowCorrelationStack.push(node.getIdentifier().getId());
                    LOG.debug("Setting correlation stack header: " + subflowCorrelationStack);
                    exchange.getIn().setHeader(SUBFLOW_CORRELATION_STACK, subflowCorrelationStack);
                }
            })
            .id(getProcessorId(flow, node, "prepareSubflowHeaders"))
            .choice()
            .id(getProcessorId(flow, node, "checkSubflowHeaders"))
            .when(header(PEER_SINK_ID).isNotNull())
            .recipientList(simple("direct:${header." + PEER_SINK_ID + "}"))
            .id(getProcessorId(flow, node, "toSubflow"));

        LOG.debug("Handling subflow source slots: " + node);
        for (Slot sourceSlot : getNode().findSlots(Slot.TYPE_SOURCE)) {

            LOG.debug("Handling subflow source slot: " + sourceSlot);
            if (sourceSlot.getPeerIdentifier() == null)
                continue;

            LOG.debug("Consuming from source peer asynchronous queue: " + sourceSlot.getPeerIdentifier().getId());
            from("seda:" + sourceSlot.getPeerIdentifier().getId() + "?multipleConsumers=true")
                .routeId(getRouteId(flow, node, sourceSlot))
                .process(exchange -> {
                    LOG.debug("Received message from asynchronous queue: " + sourceSlot.getPeerIdentifier().getId());

                    Stack<String> subflowCorrelationStack = exchange.getIn().getHeader(SUBFLOW_CORRELATION_STACK, Stack.class);
                    LOG.debug("Testing subflow '" + node + "' correlation stack: " + subflowCorrelationStack);
                    if (subflowCorrelationStack == null) {
                        LOG.warn("No correlation stack in message from asynchronous queue, dropping here: " + node);
                        return;
                    }
                    if (subflowCorrelationStack.peek().equals(node.getIdentifier().getId())) {
                        LOG.debug("Correlation found, sending copy of exchange to wires: " + node);

                        Exchange exchangeCopy = exchange.copy();
                        Stack<String> subflowCorrelationStackCopy = (Stack<String>) subflowCorrelationStack.clone();

                        LOG.debug("Popping this subflow node from the top of the stack: " + node);
                        subflowCorrelationStackCopy.pop();

                        LOG.debug("Setting correlation stack header: " + subflowCorrelationStack);
                        exchangeCopy.getIn().setHeader(SUBFLOW_CORRELATION_STACK, subflowCorrelationStackCopy);

                        LOG.debug("Finding wires of: " + sourceSlot);
                        Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getIdentifier().getId());
                        ProducerTemplate producerTemplate = getContext().createProducerTemplate();
                        for (Wire sourceWire : sourceWires) {
                            LOG.debug("Sending to direct sink destination: " + sourceWire.getSinkId());
                            producerTemplate.send("direct:" + sourceWire.getSinkId(), exchangeCopy);
                        }
                    }
                })
                .id(getProcessorId(flow, node, sourceSlot, "toSubflowWires"));

        }
    }
}
