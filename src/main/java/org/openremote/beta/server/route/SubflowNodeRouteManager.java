package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.server.route.RouteManagementUtil.getProcessorId;
import static org.openremote.beta.server.route.RouteManagementUtil.getRouteId;

public class SubflowNodeRouteManager extends NodeRouteManager {

    private static final Logger LOG = LoggerFactory.getLogger(SubflowNodeRouteManager.class);

    public static final String SUBFLOW_ID = "SUBFLOW_ID";
    public static final String PEER_SINK_ID = "PEER_SINK_ID";

    public SubflowNodeRouteManager(CamelContext context, Flow flow, Node node) {
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
                    // This will be the correlation identifier we check in asynchronous "responses"
                    exchange.getIn().setHeader(SUBFLOW_ID, getNode().getIdentifier());
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
                .choice().when(header(SUBFLOW_ID).isEqualTo(node.getIdentifier()))
                .id(getProcessorId(flow, node, sourceSlot, "checkSubflowCorrelation"))
                .process(exchange -> {
                    LOG.debug("Received message from asynchronous queue for this subflow: " + node);
                    LOG.debug("Finding wires of: " + sourceSlot);
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getIdentifier().getId());
                    ProducerTemplate producerTemplate = getContext().createProducerTemplate();
                    for (Wire sourceWire : sourceWires) {
                        LOG.debug("Sending to direct sink destination: " + sourceWire.getSinkId());
                        producerTemplate.send("direct:" + sourceWire.getSinkId(), exchange);
                    }
                })
                .id(getProcessorId(flow, node, sourceSlot, "toSubflowWires"));

        }
    }
}
