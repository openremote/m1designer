package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public class NodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NodeProcessor.class);

    final ProducerTemplate template;
    final Flow flow;
    final Node node;

    public NodeProcessor(ProducerTemplate template, Flow flow, Node node) {
        this.template = template;
        this.flow = flow;
        this.node = node;
    }

    public void process(Exchange exchange) throws Exception {
        LOG.info("### NODE PROCESSING: " + exchange.getIn().getBody());

        if (node.hasProperties()) {
            String mockEndpoint = getString(getMap(node.getProperties()), "mockEndpoint");
            if (mockEndpoint != null) {
                LOG.info("### NODE SENDING TO MOCK ENDPOINT: " + mockEndpoint);
                template.send(mockEndpoint, exchange);
            }
        }

        // Send a new exchange copy to all destination sinks (through any wires connected to any source slots)
        Slot[] sourceSlots = node.findSlots(Slot.Type.SOURCE);
        for (Slot sourceSlot : sourceSlots) {
            LOG.info("### NODE PROCESSOR USING SOURCE SLOT: " + sourceSlot);

            // Find destination wires
            Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getId());

            LOG.info("### NODE PROCESSOR DESTINATION WIRES: " + Arrays.toString(sourceWires));

            // For each wire, send a new exchange to the destination sink
            for (Wire sourceWire : sourceWires) {
                Exchange copy = copyExchange(exchange);
                Node destinationNode = flow.findOwnerNode(sourceWire.getSinkId());
                LOG.info("### NODE PROCESSOR SENDING NEW EXCHANGE TO: " + destinationNode + " ON " + flow.findSlot(sourceWire.getSinkId()));
                send(copy, destinationNode.getId(), sourceWire.getSinkId());
            }
        }
    }

    protected Exchange copyExchange(Exchange exchange) {
        Exchange copy = new DefaultExchange(exchange.getContext());
        copy.getIn().setHeaders(exchange.getIn().getHeaders());
        copy.getIn().setBody(exchange.getIn().getBody());
        return copy;
    }

    public void send(Exchange exchange, String nodeId, String sinkId) {
        exchange.getIn().setHeader(FlowProcessor.DESTINATION_NODE_ID, nodeId);
        exchange.getIn().setHeader(FlowProcessor.DESTINATION_SINK_ID, sinkId);
        exchange.getIn().setBody(exchange.getIn().getBody());
        template.send(exchange);
    }
}